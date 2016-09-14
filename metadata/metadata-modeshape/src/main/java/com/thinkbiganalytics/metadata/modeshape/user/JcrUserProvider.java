package com.thinkbiganalytics.metadata.modeshape.user;

import com.thinkbiganalytics.metadata.api.user.GroupAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserGroup.ID;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.UsersPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Provides access to {@link User} objects stored in a JCR repository.
 */
public class JcrUserProvider extends BaseJcrProvider<Object, Serializable> implements UserProvider {

    @Nonnull
    @Override
    public User ensureUser(@Nonnull final String systemName) {
        return createUser(systemName, true);
    }

    @Nonnull
    @Override
    public Optional<User> findUserBySystemName(@Nonnull final String systemName) {
        final String query = "SELECT * FROM [" + JcrUser.NODE_TYPE + "] AS user WHERE NAME() = '" + StringEscapeUtils.escapeSql(systemName) + "'";
        return Optional.ofNullable((User) findFirst(query));
    }

    @Override
    public Class<? extends User> getEntityClass() {
        return JcrUser.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrUser.class;
    }

    @Override
    public String getNodeType(@Nonnull final Class<? extends JcrEntity> jcrEntityType) {
        return JcrUser.NODE_TYPE;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.BaseProvider#resolveId(java.io.Serializable)
     */
    @Override
    public User.ID resolveId(@Nonnull final Serializable fid) {
        return new JcrUser.UserId(fid);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#resolveUserId(java.io.Serializable)
     */
    @Override
    public User.ID resolveUserId(@Nonnull Serializable id) {
        return new JcrUser.UserId(id);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#resolveGroupId(java.io.Serializable)
     */
    @Override
    public UserGroup.ID resolveGroupId(@Nonnull Serializable id) {
        return new JcrUserGroup.UserGroupId(id);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#userExists(java.lang.String)
     */
    @Override
    public boolean userExists(@Nonnull String username) {
        // TODO: is there a more efficient query than this?
        return findUserBySystemName(username).isPresent();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#findUserById(com.thinkbiganalytics.metadata.api.user.User.ID)
     */
    @Nonnull
    @Override
    public Optional<User> findUserById(@Nonnull User.ID id) {
        try {
            Node node = getSession().getNodeByIdentifier(id.toString());
            
            if (node.isNodeType(JcrUser.NODE_TYPE)) {
                return Optional.of(new JcrUser(node));
            } else {
                // TODO: should we thrown an exception if the ID is not for a user?
                return Optional.empty();
            }
        } catch (ItemNotFoundException e) { 
            return Optional.empty();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to find the user with ID: " + id, e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#getUsers()
     */
    @Nonnull
    @Override
    public Iterable<User> findUsers() {
        String query = "SELECT * FROM [" + JcrUser.NODE_TYPE + "]";
        return findIterable(query, User.class, JcrUser.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#createUser(java.lang.String)
     */
    @Nonnull
    @Override
    public User createUser(@Nonnull String username) {
        return createUser(username, false);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#findGroupById(com.thinkbiganalytics.metadata.api.user.UserGroup.ID)
     */
    @Nonnull
    @Override
    public Optional<UserGroup> findGroupById(ID id) {
        try {
            Node node = getSession().getNodeByIdentifier(id.toString());
            
            if (node.isNodeType(JcrUserGroup.NODE_TYPE)) {
                return Optional.of(new JcrUserGroup(node));
            } else {
                // TODO: should we thrown an exception if the ID is not for a group?
                return Optional.empty();
            }
        } catch (ItemNotFoundException e) { 
            return Optional.empty();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to find the group with ID: " + id, e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#findGroupByName(java.lang.String)
     */
    @Nonnull
    @Override
    public Optional<UserGroup> findGroupByName(@Nonnull String groupName) {
        final String query = "SELECT * FROM [" + JcrUserGroup.NODE_TYPE + "] AS user WHERE NAME() = '" + StringEscapeUtils.escapeSql(groupName) + "'";
        return Optional.ofNullable(findFirst(query, JcrUserGroup.class));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#getGroups()
     */
    @Nonnull
    @Override
    public Iterable<UserGroup> findGroups() {
        String query = "SELECT * FROM [" + JcrUserGroup.NODE_TYPE + "]";
        return findIterable(query, UserGroup.class, JcrUserGroup.class);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#ensureGroup(java.lang.String)
     */
    @Nonnull
    @Override
    public UserGroup ensureGroup(@Nonnull String groupName) {
        return createGroup(groupName, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#createGroup(java.lang.String)
     */
    @Nonnull
    @Override
    public UserGroup createGroup(@Nonnull String groupName) {
        return createGroup(groupName, false);
    }

    private UserGroup createGroup(String groupName, boolean ensure) {
        Session session = getSession();
        String groupPath = UsersPaths.groupPath(groupName).toString();
        
        try {
            Node groupsNode = session.getRootNode().getNode(UsersPaths.GROUPS.toString());

            if (session.getRootNode().hasNode(groupPath)) {
                if (ensure) {
                    return JcrUtil.getJcrObject(groupsNode, groupName, JcrUserGroup.class);
                } else {
                    throw new GroupAlreadyExistsException(groupName);
                }
            } else {
                return JcrUtil.getOrCreateNode(groupsNode, groupName, JcrUserGroup.NODE_TYPE, JcrUserGroup.class);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to create a new group with name: " + groupName, e);
        }
    }
    
    private User createUser(String username, boolean ensure) {
        Session session = getSession();
        String userPath = UsersPaths.userPath(username).toString();
        
        try {
            Node usersNode = session.getRootNode().getNode(UsersPaths.USERS.toString());
    
            if (session.getRootNode().hasNode(userPath)) {
                if (ensure) {
                    return JcrUtil.getJcrObject(usersNode, username, JcrUser.class);
                } else {
                    throw new UserAlreadyExistsException(username);
                }
            } else {
                return JcrUtil.getOrCreateNode(usersNode, username, JcrUser.NODE_TYPE, JcrUser.class);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to create a new user with name: " + username, e);
        }
    }

    @Override
    public void deleteGroup(@Nonnull final UserGroup group) {
        delete(group);
    }

    @Override
    public void deleteUser(@Nonnull final User user) {
        delete(user);
    }

    @Nonnull
    @Override
    public UserGroup updateGroup(@Nonnull final UserGroup group) {
        return (UserGroup)update(group);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {
        return (User)update(user);
    }
}
