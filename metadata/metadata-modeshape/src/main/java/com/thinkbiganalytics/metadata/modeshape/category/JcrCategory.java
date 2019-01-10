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
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.security.JcrCategoryAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrProperties;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IconableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IndexControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.role.RoleMembership;
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
public class JcrCategory extends JcrEntity<Category.ID> implements Category, AuditableMixin, IconableMixin, SystemEntityMixin, PropertiedMixin, IndexControlledMixin, AccessControlledMixin {

    public static final String DETAILS = "tba:details";

    public static final String CATEGORY_NAME = "tba:category";
    public static final String NODE_TYPE = "tba:category";

    private CategoryDetails details;

    // TODO: Referencing the ops access provider is kind of ugly but is needed so that 
    // a it can be passed to each feed entity when they are constructed.
    private volatile Optional<FeedOpsAccessControlProvider> opsAccessProvider = Optional.empty();
    
    
    /**
     * Constructs a JcrCategory instance starting with either its base node or a child node, and an
     * optional FeedOpsAccessControlProvider.
     * @param node a tba:category type node or one of its child nodes
     * @param accessPvdr the optional provider
     * @return a new JcrCategory instance wrapping its derived base node
     */
    public static JcrCategory createCategory(Node node, Optional<FeedOpsAccessControlProvider> accessPvdr) {
        Node baseNode = node;
        if (JcrUtil.isNodeType(node, CategoryDetails.NODE_TYPE)) {
            baseNode = JcrUtil.getParent(node);
        } else if (! JcrUtil.isNodeType(node, JcrCategory.NODE_TYPE)) {
            throw new IllegalArgumentException("Unexpected node type for category: " + node);
        }
        final Node catNode = baseNode;
        
        return accessPvdr
            .map(pvdr -> JcrUtil.createJcrObject(catNode, JcrCategory.class, pvdr))
            .orElse(JcrUtil.createJcrObject(catNode, JcrCategory.class));

    }

    public JcrCategory(Node node) {
        super(node);
    }

    public JcrCategory(Node node, FeedOpsAccessControlProvider opsAccessProvider) {
        super(node);
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin#getPropertiesObject()
     */
    @Override
    public Optional<JcrProperties> getPropertiesObject() {
        // Delegate to the details object to get the properties node that holds 
        // dynamic properties.
        return getDetails().flatMap(d -> d.getPropertiesObject());
    }

    
    // -=-=--=-=-=-=-=-=-=-=-
    

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

    @Override
    public Integer getVersion() {
        return null;
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessControlled#getLogId()
     */
    @Override
    public String getAuditId() {
        return "Category:" + getId();
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.Category#moveFeed(com.thinkbiganalytics.metadata.api.feed.Feed)
     */
    public String getFeedParentPath() {
        return JcrUtil.path(getNode(), DETAILS).toString();
    }

    public static class CategoryId extends JcrEntity.EntityId implements Category.ID {

        public CategoryId(Serializable ser) {
            super(ser);
        }
    }
}
