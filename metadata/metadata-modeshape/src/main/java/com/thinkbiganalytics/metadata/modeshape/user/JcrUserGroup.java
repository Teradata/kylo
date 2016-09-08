/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.user;

import java.io.Serializable;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public class JcrUserGroup extends AbstractJcrAuditableSystemEntity implements UserGroup {

    /** JCR node type for users */
    static final String NODE_TYPE = "tba:userGroup";

    /** The groups property from the mixin tba:userGroupable */
    private static final String GROUPS = "tba:groups";

    /**
     * @param node
     */
    public JcrUserGroup(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity#getId()
     */
    @Override
    public UserGroupId getId() {
        try {
            return new UserGroupId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity#getSystemName()
     */
    @Override
    public String getSystemName() {
        return JcrPropertyUtil.getName(this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getUsers()
     */
    @Override
    public Iterable<User> getUsers() {
        return iterateReferances(JcrUser.NODE_TYPE, User.class, JcrUser.class);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#streamAllUsers()
     */
    public Stream<User> streamAllUsers() {
        return streamAllGroups().flatMap(g -> StreamSupport.stream(g.getUsers().spliterator(), false));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#addUser(com.thinkbiganalytics.metadata.api.user.User)
     */
    @Override
    public boolean addUser(User user) {
        JcrUser jcrUser = (JcrUser) user;
        return JcrPropertyUtil.addToSetProperty(jcrUser.getNode(), GROUPS, this.node, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#removeUser(com.thinkbiganalytics.metadata.api.user.User)
     */
    @Override
    public boolean removeUser(User user) {
        JcrUser jcrUser = (JcrUser) user;
        return JcrPropertyUtil.removeFromSetProperty(jcrUser.getNode(), GROUPS, this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getGroups()
     */
    @Override
    public Iterable<UserGroup> getGroups() {
        return iterateReferances(JcrUserGroup.NODE_TYPE, UserGroup.class, JcrUserGroup.class);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#streamAllGroups()
     */
    public Stream<UserGroup> streamAllGroups() {
        return StreamSupport.stream(getGroups().spliterator(), false).flatMap(g -> g.streamAllGroups());
    
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#addGroup(com.thinkbiganalytics.metadata.api.user.UserGroup)
     */
    @Override
    public boolean addGroup(UserGroup group) {
        JcrUserGroup jcrGrp = (JcrUserGroup) group;
        return JcrPropertyUtil.addToSetProperty(jcrGrp.getNode(), GROUPS, this.node, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#removeGroup(com.thinkbiganalytics.metadata.api.user.UserGroup)
     */
    @Override
    public boolean removeGroup(UserGroup group) {
        JcrUserGroup jcrGrp = (JcrUserGroup) group;
        return JcrPropertyUtil.removeFromSetProperty(jcrGrp.getNode(), GROUPS, this.node);
    }
    
    private <C, J> Iterable<C> iterateReferances(String nodeType, Class<C> modelClass, Class<J> jcrClass) {
        return () -> {
            @SuppressWarnings("unchecked")
            Iterable<Property> propItr = () -> { 
                try {
                    return (Iterator<Property>) this.node.getWeakReferences();
                } catch (Exception e) {
                    throw new MetadataRepositoryException("Failed to retrieve the users in the group node: " + this.node, e);
                }
            };
            
            return StreamSupport.stream(propItr.spliterator(), false)
                            .map(p -> JcrPropertyUtil.getParent(p))
                            .filter(n -> JcrUtil.isNodeType(n, nodeType))
                            .map(n -> { 
                                try {
                                    @SuppressWarnings("unchecked")
                                    C entity = (C) ConstructorUtils.invokeConstructor(jcrClass, n);
                                    return entity;
                                } catch (Exception e) {
                                    throw new MetadataRepositoryException("Failed to retrieve create entity: " + jcrClass, e);
                                }
                            })
                            .iterator();
        };
    }

    /**
     * A {@link com.thinkbiganalytics.metadata.api.user.UserGroup.ID} implementation identifying a UserGroup.
     */
    static class UserGroupId extends JcrEntity.EntityId implements UserGroup.ID {

        private static final long serialVersionUID = 1L;

        UserGroupId(@Nonnull final Serializable ser) {
            super(ser);
        }
    }

}
