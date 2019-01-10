package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.catalog.DataSet;

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
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.api.feed.reindex.HistoryReindexingStatus;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrProperties;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.IndexControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.SystemEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.feed.security.JcrFeedAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.mixin.AccessControlledMixin;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrAbstractRoleMembership;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.security.role.RoleMembership;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * An implementation of {@link Feed} backed by a JCR repository.
 */
public class JcrFeed extends JcrEntity<JcrFeed.FeedId> implements Feed, PropertiedMixin, AuditableMixin, SystemEntityMixin, IndexControlledMixin, AccessControlledMixin {

    public static final String PRECONDITION_TYPE = "tba:feedPrecondition";

    public static final String NODE_TYPE = "tba:feed";

    public static final String DEPLOYED_VERSION = "tba:deployedVersion";
    public static final String SUMMARY = "tba:summary";
    public static final String DATA = "tba:data";

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
    }

    public JcrFeed(Node node, JcrCategory category, FeedOpsAccessControlProvider opsAccessProvider) {
        this(node, opsAccessProvider);
    }
    
    public Optional<Version> getDeployedVersion() {
        return Optional.ofNullable(JcrPropertyUtil.getProperty(getNode(), DEPLOYED_VERSION, null));
    }
    
    public void setDeployedVersion(Version version) {
        JcrPropertyUtil.setProperty(getNode(), DEPLOYED_VERSION, version);
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
        
        updateAllRoleMembershipPermissions(Stream.empty());
    }
    
    /**
     * Update this feed's permissions to enable only the ones allowed by all role memberships both
     * at the feed-level and the category-level.
     */
    public void updateAllRoleMembershipPermissions(Stream<RoleMembership> previousMemberships) {
        // Disable permissions from the previous memberships excluding the feed owner.
        JcrAbstractRoleMembership.disableForAll(previousMemberships, Collections.singleton(getOwner()), this.getAllowedActions());
        
        // Enable only the permissions allowed by the category and feed role memberships.
        Stream<RoleMembership> memberships = Stream.concat(getRoleMemberships().stream(), 
                                                           getCategory().getFeedRoleMemberships().stream());
        JcrAbstractRoleMembership.enableOnlyForAll(memberships, this.getAllowedActions());
    }

    // -=-=--=-=- Delegate Propertied methods to data -=-=-=-=-=-
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin#getPropertiesObject()
     */
    @Override
    public Optional<JcrProperties> getPropertiesObject() {
        // Delegate to the feed details object to get the properties node that holds 
        // dynamic properties.
        return getFeedDetails().flatMap(d -> d.getPropertiesObject());
    }
    
    // -=-=--=-=-=-=-=-=-=-=-

    
    public  void clearAdditionalProperties() {
        getFeedData().ifPresent(d -> d.clearAdditionalProperties());
    }
    
    public  Map<String, Object> getProperties() {
        return getFeedData().map(d -> d.getProperties()).orElse(Collections.emptyMap());
    }
    
    @SuppressWarnings("unchecked")
    public  <T> T getProperty(String name) {
        return (T) getFeedData().map(d -> d.getProperty(name)).orElse(null);
    }
    
    public  void setProperties(Map<String, Object> properties) {
        getFeedData().ifPresent(d -> d.setProperties(properties));
    }
    
    public  Map<String, Object> getAllProperties() {
        return getFeedData().map(d -> d.getAllProperties()).orElse(Collections.emptyMap());
    }
    
    public  <T> T getProperty(String name, T defValue) {
        return getFeedData().map(d -> d.getProperty(name, defValue)).orElse(defValue);
    }
    
    public  boolean hasProperty(String name) {
        return getFeedData().map(d -> d.hasProperty(name)).orElse(false);
    }
    
    public  <T> T getProperty(String name, Class<T> type) {
        return getFeedData().map(d -> d.getProperty(name, type)).orElse(null);
    }
    
    public  <T> T getProperty(String name, Class<T> type, T defaultValue) {
        return getFeedData().map(d -> d.getProperty(name, type, defaultValue)).orElse(defaultValue);
    }
    
    public  void setProperty(String name, Object value) {
        getFeedData().ifPresent(d -> d.setProperty(name, value));
    }
    
    public  Map<String, Object> mergeProperties(Map<String, Object> props) {
        return getFeedData().map(d -> d.mergeProperties(props)).orElse(Collections.emptyMap());
    }
    
    public  Map<String, Object> replaceProperties(Map<String, Object> props) {
        return getFeedData().map(d -> d.replaceProperties(props)).orElse(Collections.emptyMap());
    }
    
    public  void removeProperty(String key) {
        getFeedData().ifPresent(d -> d.removeProperty(key));
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

    // -=-=--=-=- Delegate AuditableMixin methods to summary -=-=-=-=-=-

    @Override
    public DateTime getModifiedTime() {
        return getFeedSummary().map(s -> s.getModifiedTime()).orElse(null);
    }

    @Override
    public String getModifiedBy() {
        return getFeedSummary().map(s -> s.getModifiedBy()).orElse(null);
    }

    // -=-=--=-=- Delegate methods to summary -=-=-=-=-=-

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

//    
//    public Category getCategory() {
//        return getFeedSummary().map(s -> s.getCategory(JcrCategory.class)).orElse(null);
//    }

    public Category getCategory() {
        Node catNode = JcrUtil.getParent(getNode());
        
        if (catNode != null) {
            return JcrCategory.createCategory(catNode, getOpsAccessProvider());
        } else {
            return null;
        }
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
    public HistoryReindexingStatus getCurrentHistoryReindexingStatus() {
        return getFeedData().map(d -> d.getCurrentHistoryReindexingStatus()).orElse(null);
    }

    @Override
    public Feed updateHistoryReindexingStatus(HistoryReindexingStatus historyReindexingStatus) {
        getFeedData().ifPresent(d -> d.updateHistoryReindexingStatus(historyReindexingStatus));
        return this;
    }

    @Override
    public List<InitializationStatus> getInitHistory() {
        return getFeedData().map(d -> d.getInitHistory()).orElse(Collections.emptyList());
    }

    @Override
    public Optional<FeedPrecondition> getPrecondition() {
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
    public FeedSource getSource(final DataSet.ID id) {
        return getFeedDetails().map(d -> d.getSource(id)).orElse(null);
    }

    @Override
    public FeedDestination getDestination(final Datasource.ID id) {
        return getFeedDetails().map(d -> d.getDestination(id)).orElse(null);
    }
    
    @Override
    public FeedDestination getDestination(final DataSet.ID id) {
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
        return getFeedDetails().map(FeedDetails::getUserProperties).orElse(new HashMap<>());
    }

    @Override
    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        getFeedDetails().ifPresent(d -> d.setUserProperties(userProperties, userFields));
    }

    public boolean isMissingRequiredProperties(@Nonnull final Set<UserFieldDescriptor> userFields) {
        return getFeedDetails().isPresent() ? getFeedDetails().get().isMissingRequiredProperties(userFields) : false;
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessControlled#getLogId()
     */
    @Override
    public String getAuditId() {
        return "Feed:" + getId();
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
                this.data = JcrUtil.getJcrObject(getNode(), DATA, FeedData.class, this);
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

    public JcrFeedSource ensureFeedSource(JcrDatasource datasource) {
        return getFeedDetails().map(d -> d.ensureFeedSource(datasource)).orElse(null);
    }
    
    public JcrFeedSource ensureFeedSource(JcrDataSet dataSet, boolean isSample) {
        return getFeedDetails().map(d -> d.ensureFeedSource(dataSet, isSample)).orElse(null);
    }

    public JcrFeedDestination ensureFeedDestination(JcrDatasource datasource) {
        return getFeedDetails().map(d -> d.ensureFeedDestination(datasource)).orElse(null);
    }
    
    public JcrFeedDestination ensureFeedDestination(JcrDataSet dataSet) {
        return getFeedDetails().map(d -> d.ensureFeedDestination(dataSet)).orElse(null);
    }

    public void removeFeedSource(JcrFeedSource source) {
        getFeedDetails().ifPresent(d -> d.removeFeedSource(source));
    }

    public void removeFeedDestination(JcrFeedDestination dest) {
        getFeedDetails().ifPresent(d -> d.removeFeedDestination(dest));
    }

    public void removeFeedSources() {
        getFeedDetails().ifPresent(d -> d.removeFeedSources());
    }

    public void removeFeedDestinations() {
        getFeedDetails().ifPresent(d -> d.removeFeedDestinations());
    }

    protected Node createNewPrecondition() {
        return getFeedDetails().map(d -> d.createNewPrecondition()).orElse(null);
    }

    public void clearSourcesAndDestinations(){
        removeFeedSources();
        removeFeedDestinations();
    }

    @Override
    public boolean isAllowIndexing() {
        return getFeedSummary().map(s -> s.isAllowIndexing()).orElse(true); //default is true
    }

    @Override
    public void setAllowIndexing(boolean allowIndexing) {
        getFeedSummary().ifPresent(s -> s.setAllowIndexing(allowIndexing));
    }
    
    public static class FeedId extends JcrEntity.EntityId implements Feed.ID {
        
        private static final long serialVersionUID = 1L;

        public FeedId(Serializable ser) {
            super(ser);
        }
    }
}
