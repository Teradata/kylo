package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;
import com.thinkbiganalytics.feedmgr.rest.model.json.UserPropertyDeserializer;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The specification for a feed and how it should interact with various components.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedMetadata implements UIFeed {

    public static enum STATE {
        NEW, ENABLED, DISABLED
    }

    private String id;

    @MetadataField(description = "The unique feed GUID")
    private String feedId;

    //pointer to RegisteredTemplate.id
    private String templateId;

    //Nifi specific
    private String inputProcessorType;
    private String templateName;
    private List<NifiProperty> properties;

    private FeedSchedule schedule;

    @MetadataField
    private String feedName;
    @MetadataField(description = "The system feed name")
    private String systemFeedName;
    @MetadataField
    private String description;

    private List<Tag> tags;
    @MetadataField
    private String dataOwner;

    private FeedCategory category;
    private TableSetup table;
    @MetadataField
    private Date createDate;
    @MetadataField
    private Date updateDate;

    private FeedDataTransformation dataTransformation;

    private boolean active = true;

    private String state;

    private String nifiProcessGroupId;

    //indicates this feed has inputPorts and is a "reusable template" for other feeds
    @JsonProperty("reusableFeed")
    private boolean isReusableFeed;

    //deprecated
    private Long version;

    private String versionName;

    // private NifiProcessGroup nifiProcessGroup;

    private RegisteredTemplate registeredTemplate;

    /**
     * User-defined business metadata
     */
    private Set<UserProperty> userProperties;

    public String getTemplateId() {
        return templateId;
    }

    @MetadataField(description = "List of Ranger/Sentry groups that you want to grant access for this feed")
    private String hadoopSecurityGroups;

    @MetadataField(description = "Type of authorization system used. NONE, RANGER, or SENTRY")
    private String hadoopAuthorizationType;

    private List<HadoopSecurityGroup> securityGroups;

    public FeedMetadata() {}

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public List<NifiProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<NifiProperty> properties) {
        this.properties = properties;
    }

    public FeedSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(FeedSchedule schedule) {
        this.schedule = schedule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputProcessorType() {
        return inputProcessorType;
    }

    public void setInputProcessorType(String inputProcessorType) {
        this.inputProcessorType = inputProcessorType;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    public FeedCategory getCategory() {
        return category;
    }

    public void setCategory(FeedCategory category) {
        this.category = category;
    }

    public String getSystemFeedName() {
        return systemFeedName;
    }

    public void setSystemFeedName(String systemFeedName) {
        this.systemFeedName = systemFeedName;
    }

    public TableSetup getTable() {
        return table;
    }

    public void setTable(TableSetup table) {
        this.table = table;
    }

    /*   public NifiProcessGroup getNifiProcessGroup() {
           return nifiProcessGroup;
       }

       public void setNifiProcessGroup(NifiProcessGroup nifiProcessGroup) {
           this.nifiProcessGroup = nifiProcessGroup;
       }
   */
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setVersion(Long version) {
        this.version = version;
        setVersionName(version + "");
    }

    public Long getVersion() {
        if (StringUtils.isNotBlank(versionName)) {
            try {
                return new Long(versionName);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else {
            return version;
        }
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return this.versionName;
    }

    @JsonIgnore
    public String getProfileTableName() {
        return this.category.getSystemName() + "." + this.getSystemFeedName() + "_profile";
    }

    @JsonIgnore
    public String getInvalidTableName() {
        return this.category.getSystemName() + "." + this.getSystemFeedName() + "_invalid";
    }

    @JsonIgnore
    public String getValidTableName() {
        return this.category.getSystemName() + "." + this.getSystemFeedName() + "_valid";
    }

    @JsonIgnore
    public String getCategoryAndFeedName() {
        return this.category.getSystemName() + "." + this.getSystemFeedName();
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public RegisteredTemplate getRegisteredTemplate() {
        return registeredTemplate;
    }

    public void setRegisteredTemplate(RegisteredTemplate registeredTemplate) {
        this.registeredTemplate = registeredTemplate;
    }

    @Override
    public String getCategoryName() {
        return this.category.getName();
    }

    @Override
    public String getCategoryId() {
        return this.category.getId();
    }

    @Override
    public String getCategoryAndFeedDisplayName() {
        return this.category.getName() + "." + this.getFeedName();
    }

    @Override
    public String getSystemCategoryName() {
        return category.getSystemName();
    }

    @Override
    public String getCategoryIcon() {
        return this.category.getIcon();
    }

    @Override
    public String getCategoryIconColor() {
        return this.category.getIconColor();
    }

    public String getNifiProcessGroupId() {
        return nifiProcessGroupId;
    }

    public void setNifiProcessGroupId(String nifiProcessGroupId) {
        this.nifiProcessGroupId = nifiProcessGroupId;
    }

    @JsonProperty("reusableFeed")
    public boolean isReusableFeed() {
        return isReusableFeed;
    }

    @JsonProperty("reusableFeed")
    public void setIsReusableFeed(boolean isReusableFeed) {
        this.isReusableFeed = isReusableFeed;
    }

    public FeedDataTransformation getDataTransformation() {
        return dataTransformation;
    }

    public void setDataTransformation(FeedDataTransformation dataTransformation) {
        this.dataTransformation = dataTransformation;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the user-defined business metadata for this feed.
     *
     * @return the user-defined properties
     * @see #setUserProperties(Set)
     * @since 0.3.0
     */
    public Set<UserProperty> getUserProperties() {
        return userProperties;
    }

    /**
     * Sets the user-defined business metadata for this feed.
     *
     * @param userProperties the user-defined properties
     * @see #getUserProperties()
     * @since 0.3.0
     */
    @JsonDeserialize(using = UserPropertyDeserializer.class)
    public void setUserProperties(final Set<UserProperty> userProperties) {
        this.userProperties = userProperties;
    }

    public List<HadoopSecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<HadoopSecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public String getHadoopSecurityGroups() {
        return hadoopSecurityGroups;
    }

    public String getHadoopAuthorizationType() {
        return hadoopAuthorizationType;
    }

    public void setHadoopAuthorizationType(String hadoopAuthorizationType) {
        this.hadoopAuthorizationType = hadoopAuthorizationType;
    }

    @JsonIgnore
    public void updateHadoopSecurityGroups() {
        if(getSecurityGroups() != null) {
            hadoopSecurityGroups = StringUtils.join(getSecurityGroups().stream().map(group -> group.getName()).collect(Collectors.toList()), ",");
        }
        else {
            hadoopSecurityGroups = "";
        }

    }
}
