package com.thinkbiganalytics.metadata.modeshape.user;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.user.GroupAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserGroup.ID;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.UsersPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Provides access to {@link User} objects stored in a JCR repository.
 */
public class JcrUserProvider extends BaseJcrProvider<Object, Serializable> implements UserProvider {
    
    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Nonnull
    @Override
    public User ensureUser(@Nonnull final String systemName) {
        return createUser(systemName, true);
    }

    @Override
    protected String getEntityQueryStartingPath() {
        return EntityUtil.pathForUsers();
    }

    @Nonnull
    @Override
    public Optional<User> findUserBySystemName(@Nonnull final String systemName) {
        String query = "SELECT * FROM [" + JcrUser.NODE_TYPE + "] AS user WHERE LOCALNAME() = $systemName ";
        query = applyFindAllFilter(query,EntityUtil.pathForUsers());
        final Map<String, String> bindParams = Collections.singletonMap("systemName", encodeUserName(systemName));
        return Optional.ofNullable(JcrQueryUtil.findFirst(getSession(), query, bindParams, getEntityClass()));
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
        String query = "SELECT * FROM [" + JcrUser.NODE_TYPE + "] ";
        query = applyFindAllFilter(query,EntityUtil.pathForUsers());
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
    public Optional<UserGroup> findGroupByName(@Nonnull final String groupName) {
        String query = "SELECT * FROM [" + JcrUserGroup.NODE_TYPE + "] AS user WHERE LOCALNAME() = $groupName ";
        query = applyFindAllFilter(query,EntityUtil.pathForGroups());
        final Map<String, String> bindParams = Collections.singletonMap("groupName", encodeGroupName(groupName));
        return Optional.ofNullable(JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrUserGroup.class));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserProvider#getGroups()
     */
    @Nonnull
    @Override
    public Iterable<UserGroup> findGroups() {
        String query = "SELECT * FROM [" + JcrUserGroup.NODE_TYPE + "] ";
        query = applyFindAllFilter(query,EntityUtil.pathForGroups());
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

    /**
     * Creates a new group with the specified name.
     *
     * @param groupName the name of the group
     * @param ensure    {@code true} to return the group if it already exists, or {@code false} to throw an exception
     * @return the group
     * @throws GroupAlreadyExistsException if the group already exists and {@code ensure} is {@code false}
     * @throws MetadataRepositoryException if the group could not be created
     */
    @Nonnull
    private UserGroup createGroup(@Nonnull final String groupName, final boolean ensure) {
        final Session session = getSession();
        final String safeGroupName = encodeGroupName(groupName);
        final String groupPath = UsersPaths.groupPath(safeGroupName).toString();

        try {
            final Node groupsNode = session.getRootNode().getNode(UsersPaths.GROUPS.toString());

            if (session.getRootNode().hasNode(groupPath)) {
                if (ensure) {
                    return JcrUtil.getJcrObject(groupsNode, safeGroupName, JcrUserGroup.class);
                } else {
                    throw new GroupAlreadyExistsException(groupName);
                }
            } else {
                return JcrUtil.getOrCreateNode(groupsNode, safeGroupName, JcrUserGroup.NODE_TYPE, JcrUserGroup.class);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to create a new group with name: " + groupName, e);
        }
    }

    /**
     * Creates a new user with the specified name.
     *
     * @param username the name of the user
     * @param ensure   {@code true} to return the user if it already exists, or {@code false} to throw an exception
     * @return the user
     * @throws UserAlreadyExistsException  if the user already exists and {@code ensure} is {@code false}
     * @throws MetadataRepositoryException if the user could not be created
     */
    @Nonnull
    private User createUser(@Nonnull final String username, final boolean ensure) {
        final Session session = getSession();
        final String safeUserName = encodeUserName(username);
        final String userPath = UsersPaths.userPath(username).toString();

        try {
            final Node usersNode = session.getRootNode().getNode(UsersPaths.USERS.toString());

            if (session.getRootNode().hasNode(userPath)) {
                if (ensure) {
                    return JcrUtil.getJcrObject(usersNode, safeUserName, JcrUser.class);
                } else {
                    throw new UserAlreadyExistsException(username);
                }
            } else {
                return JcrUtil.getOrCreateNode(usersNode, safeUserName, JcrUser.NODE_TYPE, JcrUser.class);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to create a new user with name: " + username, e);
        }
    }

    @Override
    public void deleteGroup(@Nonnull final UserGroup group) {
        actionsProvider.getAllowedActions(AllowedActions.SERVICES) 
            .ifPresent((allowed) -> allowed.disableAll(group.getPrincial()));
        delete(group);
    }

    @Override
    public void deleteUser(@Nonnull final User user) {
        delete(user);
    }

    @Nonnull
    @Override
    public UserGroup updateGroup(@Nonnull final UserGroup group) {
        return (UserGroup) update(group);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {
        return (User) update(user);
    }

    /**
     * Encodes the specified group name for use with JCR methods.
     *
     * @param groupName the raw group name
     * @return the encoded group name
     */
    @Nonnull
    private String encodeGroupName(@Nonnull final String groupName) {
        try {
            return URLEncoder.encode(groupName, JcrUserGroup.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding for system name of group: " + groupName, e);
        }
    }

    /**
     * Encodes the specified user name for use with JCR methods.
     *
     * @param username the raw user name
     * @return the encoded user name
     */
    @Nonnull
    private String encodeUserName(@Nonnull final String username) {
        try {
            return URLEncoder.encode(username, JcrUser.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding for system name of user: " + username, e);
        }
    }
}
