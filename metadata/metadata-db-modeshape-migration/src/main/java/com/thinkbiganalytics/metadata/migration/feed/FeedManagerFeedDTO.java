package com.thinkbiganalytics.metadata.migration.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A POJO for migrating feeds to a ModeShape repository.
 */
public class FeedManagerFeedDTO implements FeedManagerFeed {

    private String json;

    private String nifiProcessGroupId;

    private FeedManagerTemplate.ID templateId;

    private Category.ID categoryId;

    private ID Id;

    private String name;

    private String displayName;

    private String description;

    private boolean initialized;

    private Feed.State state = Feed.State.ENABLED;

    private Integer version;

    private DateTime createdTime;

    private DateTime modifiedTime;

    private FeedManagerCategory category;

    private FeedManagerTemplate template;

    private Set<Feed<?>> dependentFeeds;
    
    private Map<String, String> waterMarkValues = new HashMap<>();
    
    private InitializationStatus currentInitStatus = new InitializationStatus(InitializationStatus.State.PENDING);

    //template

    @Override
    public List<Feed<?>> getDependentFeeds() {
        return new ArrayList<>(this.dependentFeeds);
    }

    @Override
    public boolean addDependentFeed(Feed feed) {
        return this.dependentFeeds.add(feed);
    }

    @Override
    public boolean removeDependentFeed(Feed feed) {
        return this.dependentFeeds.remove(feed);
    }

    @Override
    public FeedManagerCategory getCategory() {
        return category;
    }

    public void setCategory(FeedManagerCategory category) {
        this.category = category;
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String getNifiProcessGroupId() {
        return nifiProcessGroupId;
    }

    @Override
    public void setNifiProcessGroupId(String nifiProcessGroupId) {
        this.nifiProcessGroupId = nifiProcessGroupId;
    }

    public FeedManagerTemplate.ID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(FeedManagerTemplate.ID templateId) {
        this.templateId = templateId;
    }

    public Category.ID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category.ID categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public ID getId() {
        return Id;
    }

    public void setId(ID id) {
        Id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getQualifiedName() {
        return getCategory().getName() + "." + getName();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    

    @Override
    public InitializationStatus getCurrentInitStatus() {
        return this.currentInitStatus;
    }

    @Override
    public void updateInitStatus(InitializationStatus status) {
        this.currentInitStatus = status;
    }

    @Override
    public List<InitializationStatus> getInitHistory() {
        return Collections.singletonList(this.currentInitStatus);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public void setTemplate(FeedManagerTemplate template) {
        this.template = template;
    }

    @Override
    public FeedManagerTemplate getTemplate() {
        return template;
    }

    @Override
    public void setVersionName(String version) {}

    @Override
    public FeedPrecondition getPrecondition() {
        return null;
    }

    @Override
    public List<? extends FeedSource> getSources() {
        return null;
    }

    @Override
    public FeedSource getSource(Datasource.ID id) {
        return null;
    }

    @Override
    public List<? extends FeedDestination> getDestinations() {
        return null;
    }

    @Override
    public FeedDestination getDestination(Datasource.ID id) {
        return null;
    }

    @Override
    public String getVersionName() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<String, Object> props) {}

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        return null;
    }

    @Override
    public void setProperty(String key, Object value) {}

    @Override
    public void removeProperty(String key) {}

    @Override
    public List<? extends ServiceLevelAgreement> getServiceLevelAgreements() {
        return null;
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return Collections.emptyMap();
    }

    @Override
    public void setUserProperties(@Nonnull Map userProperties, @Nonnull Set userFields) {}

    @Override
    public AllowedActions getAllowedActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        return null;
    }

    @Override
    public void setSecurityGroups(List securityGroups) {

    }

    @Override
    public Optional<String> getWaterMarkValue(String waterMarkName) {
        return Optional.ofNullable(this.waterMarkValues.get(waterMarkName));
    }

    @Override
    public Set<String> getWaterMarkNames() {
        return new HashSet<>(this.waterMarkValues.keySet());
    }

    @Override
    public void setWaterMarkValue(String waterMarkName, String value) {
        this.waterMarkValues.put(waterMarkName, waterMarkName);
    }

    @Override
    public List<Feed> getUsedByFeeds() {
        return null;
    }

    @Override
    public boolean addUsedByFeed(Feed feed) {
        return false;
    }

    @Override
    public boolean removeUsedByFeed(Feed feed) {
        return false;
    }
}
