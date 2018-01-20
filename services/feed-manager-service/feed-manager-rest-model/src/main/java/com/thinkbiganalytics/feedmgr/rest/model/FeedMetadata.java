package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.json.UserPropertyDeserializer;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FeedProcessingOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.metadata.FeedPropertySection;
import com.thinkbiganalytics.metadata.FeedPropertyType;
import com.thinkbiganalytics.metadata.MetadataField;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The specification for a feed and how it should interact with various components.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedMetadata extends EntityAccessControl implements UIFeed {

    boolean isNew = false;
    private String id;

    @MetadataField(description = "The unique feed GUID")
    private String feedId;

    //pointer to RegisteredTemplate.id
    private String templateId;

    //Nifi specific
    private String inputProcessorType;
    private String inputProcessorName;

    private String templateName;
    private List<NifiProperty> properties;

    @FeedPropertyType(section = FeedPropertySection.SCHEDULE)
    private FeedSchedule schedule;

    @FeedPropertyType(section = FeedPropertySection.SUMMARY)
    @MetadataField
    private String feedName;

    @FeedPropertyType(section = FeedPropertySection.SUMMARY)
    @MetadataField(description = "The system feed name")
    private String systemFeedName;

    @FeedPropertyType(section = FeedPropertySection.SUMMARY)
    @MetadataField
    private String description;

    @FeedPropertyType(section = FeedPropertySection.PROPERTIES)
    @JsonDeserialize(contentAs = DefaultTag.class)
    private List<Tag> tags;

    @FeedPropertyType(section = FeedPropertySection.PROPERTIES)
    @MetadataField
    private String dataOwner;

    private FeedCategory category;

    @FeedPropertyType(section = FeedPropertySection.TABLE_DATA)
    private TableSetup table;

    @MetadataField
    private Date createDate;
    @MetadataField
    private Date updateDate;

    @FeedPropertyType(section = FeedPropertySection.TABLE_DATA)
    private FeedDataTransformation dataTransformation;

    private boolean active = true;

    private String state;

    private String nifiProcessGroupId;

    //indicates this feed has inputPorts and is a "reusable template" for other feeds
    @JsonProperty("reusableFeed")
    private boolean isReusableFeed;
    private FeedProcessingOptions options;
    //deprecated
    private Long version;
    private String versionName;
    private RegisteredTemplate registeredTemplate;

    // private NifiProcessGroup nifiProcessGroup;
    /**
     * User-defined business metadata
     */
    @FeedPropertyType(section = FeedPropertySection.PROPERTIES)
    private Set<UserProperty> userProperties;

    @FeedPropertyType(section = FeedPropertySection.PROPERTIES)
    @MetadataField(description = "List of Ranger/Sentry groups that you want to grant access for this feed")
    private String hadoopSecurityGroups;

    @FeedPropertyType(section = FeedPropertySection.PROPERTIES)
    @MetadataField(description = "Type of authorization system used. NONE, RANGER, or SENTRY")
    private String hadoopAuthorizationType;
    private List<HadoopSecurityGroup> securityGroups;
    /**
     * List of feed IDs dependent on this feed
     */
    private List<FeedSummary> usedByFeeds;


    /**
     * List of data source dependencies.
     */
    private List<Datasource> userDatasources;

    /**
     * Additional key / value pairs for use by plugins.
     */
    private Map<String, Object> tableOption;


    public FeedMetadata() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if (properties == null) {
            properties = new ArrayList<NifiProperty>();
        }
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

    public String getInputProcessorName() {
        return inputProcessorName;
    }

    public void setInputProcessorName(String inputProcessorName) {
        this.inputProcessorName = inputProcessorName;
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

    public void setVersion(Long version) {
        this.version = version;
        setVersionName(version + "");
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @JsonIgnore
    public String getProfileTableName() {
        return getSystemCategoryName() != null ? getSystemCategoryName() + "." + this.getSystemFeedName() + "_profile" : null;
    }

    @JsonIgnore
    public String getInvalidTableName() {
        return getSystemCategoryName() != null ? getSystemCategoryName() + "." + this.getSystemFeedName() + "_invalid" : null;
    }

    @JsonIgnore
    public String getValidTableName() {
        return getSystemCategoryName() != null ? getSystemCategoryName() + "." + this.getSystemFeedName() + "_valid" : null;
    }

    @JsonIgnore
    public String getCategoryAndFeedName() {
        return getSystemCategoryName() != null ? FeedNameUtil.fullName(getSystemCategoryName(), this.getSystemFeedName()) : null;
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
        return this.category != null ? this.category.getName() : null;
    }

    @Override
    public String getCategoryId() {
        return this.category != null ? this.category.getId() : null;
    }

    @Override
    public String getCategoryAndFeedDisplayName() {
        return this.category != null ? this.category.getName() + "." + this.getFeedName() : null;
    }

    @Override
    public String getSystemCategoryName() {
        return this.category != null ? this.category.getSystemName() : null;
    }

    @Override
    public String getCategoryIcon() {
        return this.category != null ? this.category.getIcon() : null;
    }

    @Override
    public String getCategoryIconColor() {
        return this.category != null ? this.category.getIconColor() : null;
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
        if (getSecurityGroups() != null) {
            hadoopSecurityGroups = StringUtils.join(getSecurityGroups().stream().map(group -> group.getName()).collect(Collectors.toList()), ",");
        } else {
            hadoopSecurityGroups = "";
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public FeedProcessingOptions getOptions() {
        return options;
    }

    public void setOptions(FeedProcessingOptions options) {
        this.options = options;
    }

    public List<FeedSummary> getUsedByFeeds() {
        return usedByFeeds;
    }

    public void setUsedByFeeds(List<FeedSummary> usedByFeeds) {
        this.usedByFeeds = usedByFeeds;
    }

    public static enum STATE {
        NEW, ENABLED, DISABLED
    }

    @JsonIgnore
    public List<NifiProperty> getConfigurationProperties() {
        return getProperties().stream().filter(nifiProperty -> nifiProperty.isContainsConfigurationVariables()).collect(Collectors.toList());
    }

    /**
     * Return the properties for this feed that are marked as being sensitive
     */
    @JsonIgnore
    public List<NifiProperty> getSensitiveProperties() {
        return getProperties().stream()
            .filter(nifiProperty -> nifiProperty.isSensitive() && (!nifiProperty.isInputProperty() || (nifiProperty.isInputProperty()
                                                                                                       && nifiProperty.getProcessorType().equals(this.getInputProcessorType()))))
            .collect(Collectors.toList());
    }

    public List<Datasource> getUserDatasources() {
        return userDatasources;
    }

    public void setUserDatasources(List<Datasource> userDatasources) {
        this.userDatasources = userDatasources;
    }

    public Map<String, Object> getTableOption() {
        return tableOption;
    }

    public void setTableOption(Map<String, Object> tableOption) {
        this.tableOption = tableOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            active,
            category,
            createDate,
            dataOwner,
            dataTransformation,
            description,
            feedId,
            feedName,
            hadoopAuthorizationType,
            hadoopSecurityGroups,
            id,
            inputProcessorType,
            isNew,
            isReusableFeed,
            nifiProcessGroupId,
            options,
            properties,
            registeredTemplate,
            schedule,
            securityGroups,
            state,
            systemFeedName,
            table,
            tableOption,
            tags,
            templateId,
            templateName,
            updateDate,
            usedByFeeds,
            userDatasources,
            userProperties,
            version,
            versionName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        FeedMetadata other = (FeedMetadata) obj;
        
        return 
                Objects.equals(active, other.active) &&
                Objects.equals(category, other.category) &&
                Objects.equals(createDate, other.createDate) &&
                Objects.equals(dataOwner, other.dataOwner) &&
                Objects.equals(dataTransformation, other.dataTransformation) &&
                Objects.equals(description, other.description) &&
                Objects.equals(feedId, other.feedId) &&
                Objects.equals(feedName, other.feedName) &&
                Objects.equals(hadoopAuthorizationType, other.hadoopAuthorizationType) &&
                Objects.equals(hadoopSecurityGroups, other.hadoopSecurityGroups) &&
                Objects.equals(id, other.id) &&
                Objects.equals(inputProcessorType, other.inputProcessorType) &&
                Objects.equals(isNew, other.isNew) &&
                Objects.equals(isReusableFeed, other.isReusableFeed) &&
                Objects.equals(nifiProcessGroupId, other.nifiProcessGroupId) &&
                Objects.equals(options, other.options) &&
                Objects.equals(properties, other.properties) &&
                Objects.equals(registeredTemplate, other.registeredTemplate) &&
                Objects.equals(schedule, other.schedule) &&
                Objects.equals(securityGroups, other.securityGroups) &&
                Objects.equals(state, other.state) &&
                Objects.equals(systemFeedName, other.systemFeedName) &&
                Objects.equals(table, other.table) &&
                Objects.equals(tableOption, other.tableOption) &&
                Objects.equals(tags, other.tags) &&
                Objects.equals(templateId, other.templateId) &&
                Objects.equals(templateName, other.templateName) &&
                Objects.equals(updateDate, other.updateDate) &&
                Objects.equals(usedByFeeds, other.usedByFeeds) &&
                Objects.equals(userDatasources, other.userDatasources) &&
                Objects.equals(userProperties, other.userProperties) &&
                Objects.equals(version, other.version) &&
                Objects.equals(versionName, other.versionName);
    }

    
}
