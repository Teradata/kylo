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
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensiblePropertyCollection;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

/**
 * A {@link User} stored in a JCR repository.
 */
public class JcrUser extends AbstractJcrAuditableSystemEntity implements User {

    /**
     * Encoding for properties
     */
    static final String ENCODING = "UTF-8";

    /**
     * JCR node type for users
     */
    static final String NODE_TYPE = "tba:user";

    /**
     * Name of the {@code displayName} property
     */
    private static final String DISPLAY_NAME = "tba:displayName";

    /**
     * Name of the {@code email} property
     */
    private static final String EMAIL = "tba:email";

    /**
     * Name of the {@code enabled} property
     */
    private static final String ENABLED = "tba:enabled";

    /**
     * Name of the {@code groups} property
     */
    private static final String GROUPS = "tba:groups";

    /**
     * Name of the {@code password} property
     */
    private static final String PASSWORD = "tba:password";

    /**
     * Constructs a {@code JcrUser} using the specified node.
     *
     * @param node the JCR node for the user
     */
    public JcrUser(@Nonnull final Node node) {
        super(node);
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return getProperty(DISPLAY_NAME, String.class);
    }

    @Override
    public void setDisplayName(@Nullable final String displayName) {
        setProperty(DISPLAY_NAME, displayName);
    }

    @Nullable
    @Override
    public String getEmail() {
        return getProperty(EMAIL, String.class);
    }

    @Override
    public void setEmail(@Nullable final String email) {
        setProperty(EMAIL, email);
    }

    @Nonnull
    @Override
    public UserId getId() {
        try {
            return new UserId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return getProperty(ENABLED, true);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        setProperty(ENABLED, enabled);
    }

    @Nullable
    @Override
    public String getPassword() {
        return getProperty(PASSWORD, String.class);
    }

    @Override
    public void setPassword(@Nullable final String password) {
        setProperty(PASSWORD, password);
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
     * @see com.thinkbiganalytics.metadata.api.user.User#getAllContainingGroups()
     */
    @Override
    public Set<UserGroup> getAllContainingGroups() {
        return streamAllContainingGroups().collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.User#getContainingGroups()
     */
    @Override
    public Set<UserGroup> getContainingGroups() {
        return JcrPropertyUtil.<Node>getSetProperty(this.node, JcrUserGroup.GROUPS).stream()
            .filter(node -> node != null)
            .map(node -> (UserGroup) JcrUtil.toJcrObject(node, JcrUserGroup.NODE_TYPE, JcrUserGroup.class))
            .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.User#getPrincipal()
     */
    @Override
    public Principal getPrincipal() {
        return new UsernamePrincipal(getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.user.User#getGroupPrincipals()
     */
    @Nonnull
    @Override
    public Set<GroupPrincipal> getAllGroupPrincipals() {
        return streamAllContainingGroups()
            .map(group -> group.getRootPrincial())
            .collect(Collectors.toSet());
    }

    private Stream<UserGroup> streamAllContainingGroups() {
        Set<UserGroup> groups = getContainingGroups();

        return Stream.concat(groups.stream(),
                             groups.stream().flatMap(group -> group.getAllContainingGroups().stream()));
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Set<UserGroup> getGroups() {
        return getContainingGroups();
    }

    @Override
    public void setGroups(@Nonnull final Set<UserGroup> groups) {
        final JcrExtensiblePropertyCollection collection = new JcrExtensiblePropertyCollection(PropertyType.WEAKREFERENCE, groups);
        try {
            JcrPropertyUtil.setProperties(node.getSession(), node, Collections.singletonMap(GROUPS, collection));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to set groups", e);
        }
    }

    /**
     * A {@link com.thinkbiganalytics.metadata.api.user.User.ID} representing a JCR User.
     */
    static class UserId extends JcrEntity.EntityId implements User.ID {

        private static final long serialVersionUID = 1780033096808176536L;

        /**
         * Constructs a {@code UserId} with the specified username.
         *
         * @param ser the username
         */
        UserId(@Nonnull final Serializable ser) {
            super(ser);
        }
    }
}
