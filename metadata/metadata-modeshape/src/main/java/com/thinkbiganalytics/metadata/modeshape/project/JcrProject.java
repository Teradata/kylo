package com.thinkbiganalytics.metadata.modeshape.project;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IconableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JcrProject extends JcrEntity<Project.ID> implements Project, AuditableMixin, IconableMixin, SystemEntityMixin, AccessControlledMixin {

    /**
     * JCR node type for projects
     */
    public static final String NODE_TYPE = "tba:project";

    /**
     * Name of the {@code feeds} property
     */
    private static final String FEEDS = "tba:feeds";

    /**
     * Name of the {@code description} property
     */
    private static final String DESCRIPTION = "tba:description";
    
    @Inject
    private Optional<FeedOpsAccessControlProvider> opsAccessProvider = Optional.empty();

    /**
     * Constructs a {@code Project} using the specified node.
     *
     * @param node the JCR node for the user
     */
    public JcrProject(@Nonnull final Node node) {
        super(node);
    }

    @Override
    public ProjectId getId() {
        try {
            return new JcrProject.ProjectId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    
    @Override
    public String getSystemName() {
        String name = SystemEntityMixin.super.getSystemName();
        return name != null ? name : getNodeName();
    }

    @Nullable
    @Override
    public String getName() {
        return getTitle();
    }

    @Override
    public void setName(@Nullable String displayName) {
        setTitle(displayName);
    }

    @Override
    public List<Feed> getFeeds() {
        List<Feed> feeds = new ArrayList<>();
        Set<Node> feedNodes = JcrPropertyUtil.getSetProperty(getNode(), FEEDS);

        feedNodes.stream().map(depNode -> new JcrFeed(depNode, this.opsAccessProvider.orElse(null))).collect(Collectors.toCollection(() -> feeds));
        return feeds;
    }

    @Override
    public boolean addFeed(Feed feed) {
        JcrFeed jcrFeed = (JcrFeed) feed;
        Node feedNode = jcrFeed.getNode();

        return JcrPropertyUtil.addToSetProperty(getNode(), FEEDS, feedNode, true);
    }

    @Override
    public boolean removeFeed(Feed feed) {
        JcrFeed jcrFeed = (JcrFeed) feed;
        Node feedNode = jcrFeed.getNode();

        return JcrPropertyUtil.removeFromSetProperty(getNode(), FEEDS, feedNode);
    }

    @Override
    public String getDescription() {
        return getProperty(DESCRIPTION);
    }

    @Override
    public void setDescription(String description) {
        setProperty(DESCRIPTION, description);
    }

    @Override
    public Class<? extends JcrAllowedActions> getJcrAllowedActionsType() {
        return JcrProjectAllowedActions.class;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessControlled#getLogId()
     */
    @Override
    public String getAuditId() {
        return "Project:" + getId();
    }

    public static class ProjectId extends JcrEntity.EntityId implements Project.ID {

        public ProjectId(Serializable ser) {
            super(ser);
        }
    }
}
