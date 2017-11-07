package com.thinkbiganalytics.metadata.modeshape.category;

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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.security.JcrCategoryAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrEntityRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrSecurityRole;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.role.SecurityRole;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * An implementation of {@link Category} backed by a JCR repository.
 */
public class JcrCategory extends AbstractJcrAuditableSystemEntity implements Category, AccessControlledMixin {

    public static final String DETAILS = "tba:details";

    public static final String CATEGORY_NAME = "tba:category";
    public static final String NODE_TYPE = "tba:category";
    public static final String ICON = "tba:icon";
    public static final String ICON_COLOR = "tba:iconColor";

    private CategoryDetails details;

    // TODO: Referencing the ops access provider is kind of ugly but is needed so that 
    // a it can be passed to each feed entity when they are constructed.
    private volatile Optional<FeedOpsAccessControlProvider> opsAccessProvider = Optional.empty();

    public JcrCategory(Node node) {
        super(node);
    }
    
    public JcrCategory(Node node, FeedOpsAccessControlProvider opsAccessProvider) {
        super(node);
        setOpsAccessProvider(opsAccessProvider);
    }


    /**
     * This should be set after an instance of this type is created to allow the change
     * of a feed's operations access control.
     *
     * @param opsAccessProvider the opsAccessProvider to set
     */
    public void setOpsAccessProvider(FeedOpsAccessControlProvider opsAccessProvider) {
        this.opsAccessProvider = Optional.ofNullable(opsAccessProvider);
    }

    public Optional<FeedOpsAccessControlProvider> getOpsAccessProvider() {
        return this.opsAccessProvider;
    }


    public void enableAccessControl(JcrAllowedActions prototype, Principal owner, List<SecurityRole> catRoles, List<SecurityRole> feedRoles) {
        // Setup default access control for this entity
        AccessControlledMixin.super.enableAccessControl(prototype, owner, catRoles);
        
        // Setup the feed roles relationships.
        getDetails().ifPresent(d -> d.enableFeedRoles(feedRoles));
    }
    
    // -=-=--=-=- Delegate Propertied methods to details -=-=-=-=-=-
    
    @Override
    public Map<String, Object> getProperties() {
        return getDetails().map(d -> d.getProperties()).orElse(Collections.emptyMap());
    }
    
    @Override
    public void setProperties(Map<String, Object> properties) {
        getDetails().ifPresent(d -> d.setProperties(properties));
    }
    
    @Override
    public void setProperty(String name, Object value) {
        getDetails().ifPresent(d -> d.setProperty(name, value));
    }
    
    @Override
    public void removeProperty(String key) {
        getDetails().ifPresent(d -> d.removeProperty(key));
    }

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        return getDetails().map(d -> d.mergeProperties(props)).orElse(Collections.emptyMap());
    }
    
    @Override
    public Map<String, Object> replaceProperties(Map<String, Object> props) {
        return getDetails().map(d -> d.replaceProperties(props)).orElse(Collections.emptyMap());
    }

    
    
    
    public Optional<CategoryDetails> getDetails() {
        if (this.details == null) {
            if (JcrUtil.hasNode(getNode(), DETAILS)) {
                this.details = JcrUtil.getJcrObject(getNode(), DETAILS, CategoryDetails.class, this.opsAccessProvider);
                return Optional.of(this.details);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(this.details);
        }
    }

    
    public List<? extends Feed> getFeeds() {
        return getDetails().map(d -> d.getFeeds()).orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return getDetails().map(d -> d.getUserProperties()).orElse(Collections.emptyMap());
    }

    @Override
    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        getDetails().ifPresent(d -> d.setUserProperties(userProperties, userFields));
    }

    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        return getDetails().map(d -> d.getSecurityGroups()).orElse(Collections.emptyList());
    }

    public void setSecurityGroups(List<? extends HadoopSecurityGroup> hadoopSecurityGroups) {
        getDetails().ifPresent(d -> d.setSecurityGroups(hadoopSecurityGroups));
    }

    @Override
    public CategoryId getId() {
        try {
            return new JcrCategory.CategoryId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public void setDisplayName(String displayName) {
        setTitle(displayName);
    }

    public String getDescription() {
        return super.getProperty(DESCRIPTION, String.class);
    }

    public void setDescription(String description) {
        super.setProperty(DESCRIPTION, description);
    }

    public String getSystemName() {
        return super.getProperty(SYSTEM_NAME, String.class);
    }

    public void setSystemName(String systemName) {
        super.setProperty(SYSTEM_NAME, systemName);
    }

    public String getTitle() {
        return super.getProperty(TITLE, String.class);
    }

    public void setTitle(String title) {
        super.setProperty(TITLE, title);
    }
    
    @Override
    public String getIconColor() {
        return super.getProperty(ICON_COLOR, String.class, true);
    }
    
    public void setIconColor(String iconColor) {
        super.setProperty(ICON_COLOR, iconColor);
    }

    @Override
    public Integer getVersion() {
        return null;
    }

    @Override
    public String getIcon() {
        return super.getProperty(ICON, String.class, true);
    }

    public void setIcon(String icon) {
        super.setProperty(ICON, icon);
    }
    
    @Override
    public Set<RoleMembership> getFeedRoleMemberships() {
        return getDetails().map(d -> d.getFeedRoleMemberships()).orElse(null);
    }
    
    @Override
    public Optional<RoleMembership> getFeedRoleMembership(String roleName) {
        return getDetails().map(d -> d.getFeedRoleMembership(roleName)).orElse(null);
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrCategoryAllowedActions.class;
    }
    
    public String getFeedParentPath() {
        return JcrUtil.path(getNode(), DETAILS).toString();
    }

    public static class CategoryId extends JcrEntity.EntityId implements Category.ID {

        public CategoryId(Serializable ser) {
            super(ser);
        }
    }
}
