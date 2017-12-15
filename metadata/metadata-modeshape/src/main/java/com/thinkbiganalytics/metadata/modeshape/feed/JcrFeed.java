package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.feed.security.JcrFeedAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.joda.time.DateTime;

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

/**
 * An implementation of {@link Feed} backed by a JCR repository.
 */
public class JcrFeed extends AbstractJcrAuditableSystemEntity implements Feed, AccessControlledMixin {

    public static final String PRECONDITION_TYPE = "tba:feedPrecondition";

    public static final String NODE_TYPE = "tba:feed";

    public static final String SUMMARY = "tba:summary";
    public static final String DATA = "tba:data";

    private Node summaryNode;
    private FeedSummary summary;
    private FeedData data;

    // TODO: Referencing the ops access provider is kind of ugly but is needed so that 
    // a change to the feed's allowed accessions for ops access get's propagated to the JPA table.
    private volatile Optional<FeedOpsAccessControlProvider> opsAccessProvider = Optional.empty();

    public JcrFeed(Node node) {
        super(node);
    }
    
    public JcrFeed(Node feedNode, Node summaryNode, FeedOpsAccessControlProvider opsAccessProvider) {
        this(feedNode, opsAccessProvider);
        // The summary node will be different (not a child of the feed node) if this is a past version,
        // so it must be supplied at construction.
        this.summary = JcrUtil.getJcrObject(summaryNode, FeedSummary.class, this);
    }

    public JcrFeed(Node node, FeedOpsAccessControlProvider opsAccessProvider) {
        super(node);
        setOpsAccessProvider(opsAccessProvider);
    }

    public JcrFeed(Node node, JcrCategory category) {
        this(node, (FeedOpsAccessControlProvider) null);
        if (category != null) {
            getFeedSummary().ifPresent(s -> s.setProperty(FeedSummary.CATEGORY, category));
        }
    }

    public JcrFeed(Node node, JcrCategory category, FeedOpsAccessControlProvider opsAccessProvider) {
        this(node, opsAccessProvider);
        if (category != null) {
            getFeedSummary().ifPresent(s -> s.setProperty(FeedSummary.CATEGORY, category));
        }
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin#enableAccessControl(com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions, java.security.Principal, java.util.List)
     */
    @Override
    public void enableAccessControl(JcrAllowedActions prototype, Principal owner, List<SecurityRole> roles) {
        AccessControlledMixin.super.enableAccessControl(prototype, owner, roles);
        
        JcrAbstractRoleMembership.enableOnlyForAll(getCategory().getFeedRoleMemberships().stream(), this.getAllowedActions());
    }

    // -=-=--=-=- Delegate Propertied methods to data -=-=-=-=-=-

    @Override
    public Map<String, Object> getProperties() {
        return getFeedData().map(d -> d.getProperties()).orElse(Collections.emptyMap());
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        getFeedData().ifPresent(d -> d.setProperties(properties));
    }

    @Override
    public void setProperty(String name, Object value) {
        getFeedData().ifPresent(d -> d.setProperty(name, value));
    }

    @Override
    public void removeProperty(String key) {
        getFeedData().ifPresent(d -> d.removeProperty(key));
    }

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        return getFeedData().map(d -> d.mergeProperties(props)).orElse(Collections.emptyMap());
    }

    @Override
    public Map<String, Object> replaceProperties(Map<String, Object> props) {
        return getFeedData().map(d -> d.replaceProperties(props)).orElse(Collections.emptyMap());
    }

    // -=-=--=-=- Delegate taggable methods to summary -=-=-=-=-=-

    @Override
    public Set<String> addTag(String tag) {
        return getFeedSummary().map(s -> s.addTag(tag)).orElse(Collections.emptySet());
    }

    @Override
    public Set<String> getTags() {
        return getFeedSummary().map(s -> s.getTags()).orElse(Collections.emptySet());
    }

    @Override
    public void setTags(Set<String> tags) {
        getFeedSummary().ifPresent(s -> s.setTags(tags));
    }

    @Override
    public boolean hasTag(String tag) {
        return getFeedSummary().map(s -> s.hasTag(tag)).orElse(false);
    }

    // -=-=--=-=- Delegate AbstractJcrAuditableSystemEntity methods to summary -=-=-=-=-=-

    @Override
    public DateTime getCreatedTime() {
        return getFeedSummary().map(s -> s.getCreatedTime()).orElse(null);
    }

    @Override
    public void setCreatedTime(DateTime createdTime) {
        getFeedSummary().ifPresent(s -> s.setCreatedTime(createdTime));
    }

    @Override
    public DateTime getModifiedTime() {
        return getFeedSummary().map(s -> s.getModifiedTime()).orElse(null);
    }

    @Override
    public void setModifiedTime(DateTime modifiedTime) {
        getFeedSummary().ifPresent(s -> s.setModifiedTime(modifiedTime));
    }

    @Override
    public String getCreatedBy() {
        return getFeedSummary().map(s -> s.getCreatedBy()).orElse(null);
    }

    @Override
    public String getModifiedBy() {
        return getFeedSummary().map(s -> s.getModifiedBy()).orElse(null);
    }

    // -=-=--=-=- Delegate AbstractJcrSystemEntity methods to summary -=-=-=-=-=-

    @Override
    public String getDescription() {
        return getFeedSummary().map(s -> s.getDescription()).orElse(null);
    }

    @Override
    public void setDescription(String description) {
        getFeedSummary().ifPresent(s -> s.setDescription(description));
    }

    @Override
    public String getSystemName() {
        return getFeedSummary().map(s -> s.getSystemName()).orElse(null);
    }

    @Override
    public void setSystemName(String systemName) {
        getFeedSummary().ifPresent(s -> s.setSystemName(systemName));
    }

    @Override
    public String getTitle() {
        return getFeedSummary().map(s -> s.getTitle()).orElse(null);
    }

    @Override
    public void setTitle(String title) {
        getFeedSummary().ifPresent(s -> s.setTitle(title));
    }


    public Category getCategory() {
        return getFeedSummary().map(s -> s.getCategory(JcrCategory.class)).orElse(null);
    }

    public FeedManagerTemplate getTemplate() {
        return getFeedDetails().map(d -> d.getTemplate()).orElse(null);
    }

    public void setTemplate(FeedManagerTemplate template) {
        getFeedDetails().ifPresent(d -> d.setTemplate(template));
    }

    public List<? extends FeedSource> getSources() {
        return getFeedDetails().map(d -> d.getSources()).orElse(Collections.emptyList());
    }

    public List<? extends FeedDestination> getDestinations() {
        return getFeedDetails().map(d -> d.getDestinations()).orElse(Collections.emptyList());
    }

    @Override
    public String getName() {
        return getSystemName();
    }

    @Override
    public String getQualifiedName() {
        return getCategory().getSystemName() + "." + getName();
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public void setDisplayName(String name) {
        setTitle(name);
    }

    @Override
    public Feed.State getState() {
        return getFeedData().map(d -> d.getState()).orElse(null);
    }

    @Override
    public void setState(State state) {
        getFeedData().ifPresent(d -> d.setState(state));
    }

    @Override
    public boolean isInitialized() {
        return getFeedData().map(d -> d.isInitialized()).orElse(null);
    }

    @Override
    public InitializationStatus getCurrentInitStatus() {
        return getFeedData().map(d -> d.getCurrentInitStatus()).orElse(null);
    }

    @Override
    public void updateInitStatus(InitializationStatus status) {
        getFeedData().ifPresent(d -> d.updateInitStatus(status));
    }

    @Override
    public List<InitializationStatus> getInitHistory() {
        return getFeedData().map(d -> d.getInitHistory()).orElse(Collections.emptyList());
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return getFeedDetails().map(d -> d.getPrecondition()).orElse(null);
    }

    public void setPrecondition(JcrServiceLevelAgreement sla) {
//        Node precondNode
    }

    @Override
    public Set<String> getWaterMarkNames() {
        return getFeedData().map(d -> d.getWaterMarkNames()).orElse(Collections.emptySet());
    }

    @Override
    public Optional<String> getWaterMarkValue(String waterMarkName) {
        return getFeedData().flatMap(d -> d.getWaterMarkValue(waterMarkName));
    }

    @Override
    public void setWaterMarkValue(String waterMarkName, String value) {
        getFeedData().ifPresent(d -> d.setWaterMarkValue(waterMarkName, value));
    }

    @Override
    public List<Feed> getDependentFeeds() {
        return getFeedDetails().map(d -> d.getDependentFeeds()).orElse(Collections.emptyList());
    }

    @Override
    public boolean addDependentFeed(Feed feed) {
        return getFeedDetails().map(d -> d.addDependentFeed(feed)).orElse(false);
    }

    @Override
    public boolean removeDependentFeed(Feed feed) {
        return getFeedDetails().map(d -> d.removeDependentFeed(feed)).orElse(false);
    }

    @Override
    public List<Feed> getUsedByFeeds() {
        return getFeedDetails().map(d -> d.getUsedByFeeds()).orElse(Collections.emptyList());
    }

    @Override
    public boolean addUsedByFeed(Feed feed) {
        return getFeedDetails().map(d -> d.addUsedByFeed(feed)).orElse(false);
    }

    @Override
    public boolean removeUsedByFeed(Feed feed) {
        return getFeedDetails().map(d -> d.removeUsedByFeed(feed)).orElse(false);
    }

    @Override
    public FeedSource getSource(final Datasource.ID id) {
        return getFeedDetails().map(d -> d.getSource(id)).orElse(null);
    }

    @Override
    public FeedDestination getDestination(final Datasource.ID id) {
        return getFeedDetails().map(d -> d.getDestination(id)).orElse(null);
    }

    public String getSchedulePeriod() {
        return getFeedData().map(d -> d.getSchedulePeriod()).orElse(null);
    }

    public void setSchedulePeriod(String schedulePeriod) {
        getFeedData().ifPresent(d -> d.setSchedulePeriod(schedulePeriod));
    }

    public String getScheduleStrategy() {
        return getFeedData().map(d -> d.getScheduleStrategy()).orElse(null);
    }

    public void setScheduleStrategy(String scheduleStrategy) {
        getFeedData().ifPresent(d -> d.setScheduleStrategy(scheduleStrategy));
    }

    public List<ServiceLevelAgreement> getServiceLevelAgreements() {
        return getFeedDetails().map(d -> d.getServiceLevelAgreements()).orElse(Collections.emptyList());
    }

    public void setServiceLevelAgreements(List<? extends ServiceLevelAgreement> serviceLevelAgreements) {
        getFeedDetails().ifPresent(d -> d.setServiceLevelAgreements(serviceLevelAgreements));
    }

    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        return getFeedData().map(d -> d.getSecurityGroups()).orElse(Collections.emptyList());
    }

    public void setSecurityGroups(List<? extends HadoopSecurityGroup> hadoopSecurityGroups) {
        getFeedData().ifPresent(d -> d.setSecurityGroups(hadoopSecurityGroups));
    }

    public void removeServiceLevelAgreement(ServiceLevelAgreement.ID id) {
        getFeedDetails().ifPresent(d -> d.removeServiceLevelAgreement(id));
    }

    public boolean addServiceLevelAgreement(ServiceLevelAgreement sla) {
        return getFeedDetails().map(d -> d.addServiceLevelAgreement(sla)).orElse(false);
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return JcrPropertyUtil.getUserProperties(node);
    }

    @Override
    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        JcrPropertyUtil.setUserProperties(node, userFields, userProperties);
    }

    @Override
    public String getJson() {
        return getFeedDetails().map(d -> d.getJson()).orElse(null);
    }

    @Override
    public void setJson(String json) {
        getFeedDetails().ifPresent(d -> d.setJson(json));

    }

    @Override
    public String getNifiProcessGroupId() {
        return getFeedDetails().map(d -> d.getNifiProcessGroupId()).orElse(null);
    }

    @Override
    public void setNifiProcessGroupId(String id) {
        getFeedDetails().ifPresent(d -> d.setNifiProcessGroupId(id));
    }
    
    public Set<RoleMembership> getInheritedRoleMemberships() {
        return getCategory().getFeedRoleMemberships();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin#getJcrAllowedActions()
     */
    @Override
    public JcrAllowedActions getJcrAllowedActions() {
        Node allowedNode = JcrUtil.getNode(getNode(), JcrAllowedActions.NODE_NAME);
        return JcrUtil.createJcrObject(allowedNode, getJcrAllowedActionsType(), this.opsAccessProvider.orElse(null));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin#getJcrAllowedActionsType()
     */
    @Override
    public Class<JcrFeedAllowedActions> getJcrAllowedActionsType() {
        return JcrFeedAllowedActions.class;
    }

    public Optional<FeedSummary> getFeedSummary() {
        if (this.summary == null) {
            if (JcrUtil.hasNode(getNode(), SUMMARY)) {
                this.summary = JcrUtil.getJcrObject(getNode(), SUMMARY, FeedSummary.class, this);
                return Optional.of(this.summary);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(this.summary);
        }
    }

    public Optional<FeedDetails> getFeedDetails() {
        Optional<FeedSummary> summary = getFeedSummary();

        if (summary.isPresent()) {
            return summary.get().getFeedDetails();
        } else {
            return Optional.empty();
        }
    }

    public Optional<FeedData> getFeedData() {
        if (this.data == null) {
            if (JcrUtil.hasNode(getNode(), DATA)) {
                this.data = JcrUtil.getJcrObject(getNode(), DATA, FeedData.class);
                return Optional.of(this.data);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(this.data);
        }
    }

    @Override
    public FeedId getId() {
        try {
            return new JcrFeed.FeedId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    protected JcrFeedSource ensureFeedSource(JcrDatasource datasource) {
        return getFeedDetails().map(d -> d.ensureFeedSource(datasource)).orElse(null);
    }

    protected JcrFeedDestination ensureFeedDestination(JcrDatasource datasource) {
        return getFeedDetails().map(d -> d.ensureFeedDestination(datasource)).orElse(null);
    }

    protected void removeFeedSource(JcrFeedSource source) {
        getFeedDetails().ifPresent(d -> d.removeFeedSource(source));
    }

    protected void removeFeedDestination(JcrFeedDestination dest) {
        getFeedDetails().ifPresent(d -> d.removeFeedDestination(dest));
    }

    protected void removeFeedSources() {
        getFeedDetails().ifPresent(d -> d.removeFeedSources());
    }

    protected void removeFeedDestinations() {
        getFeedDetails().ifPresent(d -> d.removeFeedDestinations());
    }

    protected Node createNewPrecondition() {
        return getFeedDetails().map(d -> d.createNewPrecondition()).orElse(null);
    }

    public void clearSourcesAndDestinations(){
        removeFeedSources();
        removeFeedDestinations();
    }

    public static class FeedId extends JcrEntity.EntityId implements Feed.ID {

        public FeedId(Serializable ser) {
            super(ser);
        }
    }
}
