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

import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.GroupPrincipal;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrUserGroup extends AbstractJcrAuditableSystemEntity implements UserGroup {

    /**
     * JCR node type for users
     */
    public static final String NODE_TYPE = "tba:userGroup";

    /**
     * The groups property from the mixin tba:userGroupable
     */
    public static final String GROUPS = "tba:groups";

    /**
     * Encoding for properties
     */
    static final String ENCODING = "UTF-8";

    /**
     * Name of the {@code enabled} property
     */
    private static final String ENABLED = "tba:enabled";

    /**
     * @param node
     */
    public JcrUserGroup(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity#getId()
     */
    @Nonnull
    @Override
    public UserGroupId getId() {
        try {
            return new UserGroupId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Nonnull
    @Override
    public String getSystemName() {
        try {
            return URLDecoder.decode(JcrPropertyUtil.getName(this.node), ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding for system name of user: " + this.node, e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return getProperty(ENABLED, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled) {
        setProperty(ENABLED, enabled);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getUsers()
     */
    @Nonnull
    @Override
    public Iterable<User> getUsers() {
        return iterateReferances(JcrUser.NODE_TYPE, User.class, JcrUser.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#streamAllUsers()
     */
    @Nonnull
    public Stream<User> streamAllUsers() {
        return streamAllGroups().flatMap(g -> StreamSupport.stream(g.getUsers().spliterator(), false));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#addUser(com.thinkbiganalytics.metadata.api.user.User)
     */
    @Override
    public boolean addUser(@Nonnull User user) {
        JcrUser jcrUser = (JcrUser) user;
        return JcrPropertyUtil.addToSetProperty(jcrUser.getNode(), GROUPS, this.node, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#removeUser(com.thinkbiganalytics.metadata.api.user.User)
     */
    @Override
    public boolean removeUser(@Nonnull User user) {
        JcrUser jcrUser = (JcrUser) user;
        return JcrPropertyUtil.removeFromSetProperty(jcrUser.getNode(), GROUPS, this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getContainingGroups()
     */
    @Nonnull
    @Override
    public Set<UserGroup> getContainingGroups() {
        return streamContainingGroupNodes(this.node)
            .map(node -> (UserGroup) JcrUtil.toJcrObject(node, JcrUserGroup.NODE_TYPE, JcrUserGroup.class))
            .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getAllContainingGroups()
     */
    @Override
    public Set<UserGroup> getAllContainingGroups() {
        return streamAllContainingGroupNodes(this.node)
            .map(node -> JcrUtil.toJcrObject(node, JcrUserGroup.NODE_TYPE, JcrUserGroup.class))
            .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getGroups()
     */
    @Nonnull
    @Override
    public Iterable<UserGroup> getGroups() {
        return iterateReferances(JcrUserGroup.NODE_TYPE, UserGroup.class, JcrUserGroup.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#streamAllGroups()
     */
    @Nonnull
    public Stream<UserGroup> streamAllGroups() {
        return StreamSupport.stream(getGroups().spliterator(), false).flatMap(g -> g.streamAllGroups());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#addGroup(com.thinkbiganalytics.metadata.api.user.UserGroup)
     */
    @Override
    public boolean addGroup(@Nonnull UserGroup group) {
        JcrUserGroup jcrGrp = (JcrUserGroup) group;
        return JcrPropertyUtil.addToSetProperty(jcrGrp.getNode(), GROUPS, this.node, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#removeGroup(com.thinkbiganalytics.metadata.api.user.UserGroup)
     */
    @Override
    public boolean removeGroup(@Nonnull UserGroup group) {
        JcrUserGroup jcrGrp = (JcrUserGroup) group;
        return JcrPropertyUtil.removeFromSetProperty(jcrGrp.getNode(), GROUPS, this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getPrincial()
     */
    @Override
    public GroupPrincipal getPrincial() {
        Set<Principal> members = StreamSupport.stream(getGroups().spliterator(), false)
            .map(g -> g.getPrincial())
            .collect(Collectors.toSet());

        return new GroupPrincipal(getSystemName(), members);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.UserGroup#getRootPrincial()
     */
    @Override
    public GroupPrincipal getRootPrincial() {
        return new GroupPrincipal(getSystemName());
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

    private Stream<Node> streamContainingGroupNodes(Node groupNode) {
        return JcrPropertyUtil.<Node>getSetProperty(groupNode, JcrUserGroup.GROUPS).stream();
    }

    private Stream<Node> streamAllContainingGroupNodes(Node groupNode) {
        Set<Node> referenced = JcrPropertyUtil.<Node>getSetProperty(groupNode, JcrUserGroup.GROUPS);

        return Stream.concat(referenced.stream(),
                             referenced.stream().flatMap(node -> streamAllContainingGroupNodes(node)));
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
