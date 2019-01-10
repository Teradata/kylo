package com.thinkbiganalytics.feedmgr.service.feed;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersionDifference;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.reindex.HistoryReindexingStatus;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Transforms feeds between Feed Manager and Metadata formats.
 */
public class FeedModelTransform {

    private static final Logger log = LoggerFactory.getLogger(FeedModelTransform.class);

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DataSourceProvider dataSourceProvider;
    
    @Inject
    private DataSetProvider dataSetProvider;
    
    @Inject
    private SecurityModelTransform securityTransform;

    @Inject
    private TemplateModelTransform templateModelTransform;

    @Inject
    private CategoryModelTransform categoryModelTransform;
    
    @Inject
    private CatalogModelTransform catalogModelTransform;

    @Inject
    private HiveService hiveService;

    @Inject
    private HadoopSecurityGroupProvider hadoopSecurityGroupProvider;

    @Inject
    private EncryptionService encryptionService;

    @Inject
    private AccessController accessController;

    /**
     *
     * @param feedMetadata
     */
    private void prepareForSave(FeedMetadata feedMetadata) {

        if (feedMetadata.getTable() != null) {
            feedMetadata.getTable().simplifyFieldPoliciesForSerialization();
        }
        feedMetadata.setRegisteredTemplate(null);

        //reset all those properties that contain config variables back to the string with the config options
        feedMetadata.getProperties().stream().filter(property -> property.isContainsConfigurationVariables()).forEach(property -> property.setValue(property.getTemplateValue()));
        //reset all sensitive properties
        //ensure its encrypted
        encryptSensitivePropertyValues(feedMetadata);


    }

    private void clearSensitivePropertyValues(FeedMetadata feedMetadata) {
        feedMetadata.getProperties().stream().filter(property -> property.isSensitive()).forEach(nifiProperty -> nifiProperty.setValue(""));
    }

    public void encryptSensitivePropertyValues(FeedMetadata feedMetadata) {
        List<String> encrypted = new ArrayList<>();
        feedMetadata.getSensitiveProperties().stream().forEach(nifiProperty -> {
            nifiProperty.setValue(encryptionService.encrypt(nifiProperty.getValue()));
            encrypted.add(nifiProperty.getValue());
        });
        int i = 0;
    }

    public void decryptSensitivePropertyValues(FeedMetadata feedMetadata) {
        List<String> decrypted = new ArrayList<>();
        feedMetadata.getProperties().stream().filter(property -> property.isSensitive()).forEach(nifiProperty -> {
            try {
                String decryptedValue = encryptionService.decrypt(nifiProperty.getValue());
                nifiProperty.setValue(decryptedValue);
                decrypted.add(decryptedValue);
            } catch (Exception e) {

            }
        });
        int i = 0;
    }

    /**
     * Convert a spring-data Page to a SearchResult UI object
     */
    public SearchResult toSearchResult(Page<UIFeed> page) {
        SearchResult searchResult = new SearchResultImpl();
        searchResult.setData(page.getContent());
        searchResult.setRecordsTotal(page.getTotalElements());
        searchResult.setRecordsFiltered(page.getTotalElements());
        return searchResult;
    }

    /**
     * Transforms the specified Feed Manager feed to a Metadata feed.
     *
     * @param feedMetadata the Feed Manager feed
     * @return the Metadata feed
     */
    @Nonnull
    public Feed feedToDomain(@Nonnull final FeedMetadata feedMetadata) {
        //resolve the id
        Feed.ID inputId = feedMetadata.getId() != null ? feedProvider.resolveId(feedMetadata.getId()) : null;
        Feed domain = inputId != null ? feedProvider.findById(inputId) : null;

        FeedCategory restCategoryModel = feedMetadata.getCategory();
        Category category = restCategoryModel != null ? categoryProvider.findById(categoryProvider.resolveId(restCategoryModel.getId())) : null;

        if (domain == null) {
            //ensure the Category exists
            if (category == null) {
                final String categoryId = (restCategoryModel != null) ? restCategoryModel.getId() : "(null)";
                throw new RuntimeException("Category cannot be found while creating feed " + feedMetadata.getSystemFeedName() + ".  Category Id is " + categoryId);
            }
            domain = feedProvider.ensureFeed(category.getId(), feedMetadata.getSystemFeedName());
            inputId = domain.getId();
            Feed.State state = Feed.State.valueOf(feedMetadata.getState());
            domain.setState(state);

            //reassign the domain data back to the ui model....
            feedMetadata.setFeedId(inputId.toString());
            feedMetadata.setState(state.name());

        }
        
        final Feed.ID domainId = inputId;
        
        // Check if the category is changing
        if (category != null && ! category.getId().equals(domain.getCategory().getId())) {
            domain = feedProvider.moveFeed(domain, category);
        }
        
        // Check if the feed system name is changing
        if (! domain.getSystemName().equals(feedMetadata.getSystemFeedName())) {
            domain = feedProvider.changeSystemName(domain, feedMetadata.getSystemFeedName());
            // Make sure the table schema name matches the system name.
            if (feedMetadata.getTable() != null && feedMetadata.getTable().getTableSchema() != null) {
                feedMetadata.getTable().getTableSchema().setName(feedMetadata.getSystemFeedName());
            }
        }
        
        domain.setDisplayName(feedMetadata.getFeedName());
        if(feedMetadata.getDescription() == null){
            feedMetadata.setDescription("");
        }
        domain.setDescription(feedMetadata.getDescription());
        domain.setAllowIndexing(feedMetadata.isAllowIndexing());

        if (StringUtils.isNotBlank(feedMetadata.getHistoryReindexingStatus())) {
            domain.updateHistoryReindexingStatus(
                new HistoryReindexingStatus(com.thinkbiganalytics.metadata.api.feed.reindex.HistoryReindexingState.valueOf(feedMetadata.getHistoryReindexingStatus())));
        }

        feedMetadata.setId(domain.getId().toString());

        if (StringUtils.isNotBlank(feedMetadata.getState())) {
            Feed.State state = Feed.State.valueOf(feedMetadata.getState().toUpperCase());
            domain.setState(state);
        }

        domain.setNifiProcessGroupId(feedMetadata.getNifiProcessGroupId());

        //clear out the state as that
        RegisteredTemplate template = feedMetadata.getRegisteredTemplate();
        prepareForSave(feedMetadata);

        feedMetadata.setRegisteredTemplate(template);
        if (domain.getTemplate() == null) {
            FeedManagerTemplate.ID templateId = templateProvider.resolveId(feedMetadata.getTemplateId());
            FeedManagerTemplate domainTemplate = templateProvider.findById(templateId);
            domain.setTemplate(domainTemplate);
        }

        // Set user-defined properties
        if (feedMetadata.getUserProperties() != null) {
            final Set<UserFieldDescriptor> userFields = getUserFields(category);
            domain.setUserProperties(UserPropertyTransform.toMetadataProperties(feedMetadata.getUserProperties()), userFields);
        }

        // Set the hadoop security groups
        final List<HadoopSecurityGroup> securityGroups = new ArrayList<>();
        if (feedMetadata.getSecurityGroups() != null) {

            for (com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup securityGroup : feedMetadata.getSecurityGroups()) {
                JcrHadoopSecurityGroup hadoopSecurityGroup = (JcrHadoopSecurityGroup) hadoopSecurityGroupProvider.ensureSecurityGroup(securityGroup.getName());
                hadoopSecurityGroup.setGroupId(securityGroup.getId());
                hadoopSecurityGroup.setDescription(securityGroup.getDescription());
                securityGroups.add(hadoopSecurityGroup);
            }

        }
        domain.setSecurityGroups(securityGroups);
        domain.setVersionName(domain.getVersionName());
        
        if (feedMetadata.getTags() != null) {
            domain.setTags(feedMetadata.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
        }

        // Create a new feed metadata stripped of any excess data that does 
        // not need to be serialized and stored in the feed domain entity.
        FeedMetadata stripped = stripMetadata(feedMetadata);
        domain.setJson(ObjectMapperSerializer.serialize(stripped));
        return domain;
    }
    
    
    /**
     * Clean out any excess or redundant data that should not be serialized and stored
     * with the feed domain entity as JSON.
     * @param source the source metadata
     * @return a new metadata instance without the excess data
     */
    private FeedMetadata stripMetadata(FeedMetadata source) {
        FeedMetadata result = new FeedMetadata();

        result.setDataOwner(source.getDataOwner());
        result.setDataTransformation(source.getDataTransformation());
        result.setHadoopAuthorizationType(source.getHadoopAuthorizationType());
        result.setInputProcessorType(source.getInputProcessorType());
        result.setInputProcessorName(source.getInputProcessorName());
        result.setIsReusableFeed(source.isReusableFeed());
        result.setNifiProcessGroupId(source.getNifiProcessGroupId());
        result.setOptions(source.getOptions());
        result.setProperties(source.getProperties());
        result.setSchedule(source.getSchedule());
        result.setTable(source.getTable());
        result.setTableOption(source.getTableOption());
        if(source.getSourceDataSets() != null) {
            result.setSourceDataSets(source.getSourceDataSets());
        }
        if(source.getSampleDataSet() != null) {
            result.setSampleDataSet(source.getSampleDataSet());
        }
        result.setUiState(source.getUiState());
        result.setActive(source.isActive());
        return result;
    }

    /**
     * Transforms the specified domain feed versions to a FeedVersions.
     *
     * @param feedId the feed id
     * @return the Feed Manager feed
     */
    @Nonnull
    public FeedVersions domainToFeedVersions(@Nonnull final List<EntityVersion<Feed.ID, Feed>> versions, @Nonnull final Feed.ID feedId, final EntityVersion.ID deployedId) {
       return domainToFeedVersions(versions,feedId,deployedId,null);
    }

    /**
     *
     * @param versions
     * @param feedId
     * @param deployedId
     * @param allowedActions
     * @return
     */
    public FeedVersions domainToFeedVersions(@Nonnull final List<EntityVersion<Feed.ID, Feed>> versions, @Nonnull final Feed.ID feedId, final EntityVersion.ID deployedId, ActionGroup allowedActions) {
        String deployed = deployedId != null ? deployedId.toString() : null;
        FeedVersions feedVersions = new FeedVersions(feedId.toString(), deployed);
        versions.forEach(domainVer -> feedVersions.getVersions().add(domainToFeedVersion(domainVer,allowedActions)));
        return feedVersions;
    }
    
    @Nonnull
    public com.thinkbiganalytics.feedmgr.rest.model.EntityVersion domainToFeedVersion(EntityVersion<Feed.ID, Feed> domainVer) {
      return domainToFeedVersion(domainVer,null);
    }

    public com.thinkbiganalytics.feedmgr.rest.model.EntityVersion domainToFeedVersion(EntityVersion<Feed.ID, Feed> domainVer, ActionGroup actionGroup) {
        com.thinkbiganalytics.feedmgr.rest.model.EntityVersion version
            = new com.thinkbiganalytics.feedmgr.rest.model.EntityVersion(domainVer.getId().toString(),
                                                                         domainVer.getName(),
                                                                         domainVer.getCreatedDate().toDate(),
                                                                         domainVer.getChangeComment().map(chg -> chg.getUser().getName()).orElse(""),
                                                                         domainVer.getChangeComment().map(chg -> chg.getComment()).orElse(""),
                                                                         domainVer.getEntityId().toString());
        domainVer.getEntity().ifPresent(feed -> version.setEntity(domainToFeedMetadata(feed, actionGroup)));
        return version;
    }

    /**
     * Transforms the specified Metadata feed to a Feed Manager feed.
     *
     * @param domain the Metadata feed
     * @return the Feed Manager feed
     */
    @Nonnull
    public FeedMetadata domainToFeedMetadata(@Nonnull final Feed domain) {
        return domainToFeedMetadata(domain,null, (ActionGroup) null);
    }

    public FeedMetadata domainToFeedMetadata(@Nonnull final Feed domain, ActionGroup actionGroup) {
        return domainToFeedMetadata(domain, null,actionGroup);
    }
    /**
     * Transforms the specified Metadata feeds to Feed Manager feeds.
     *
     * @param domain the Metadata feeds
     * @return the Feed Manager feeds
     */
    @Nonnull
    public List<FeedMetadata> domainToFeedMetadata(@Nonnull final Collection<? extends Feed> domain) {
        final Map<Category, Set<UserFieldDescriptor>> userFieldMap = Maps.newHashMap();
        return domain.stream().map(f -> domainToFeedMetadata(f, userFieldMap)).collect(Collectors.toList());
    }

    public FeedMetadata deserializeFeedMetadata(Feed domain, boolean clearSensitiveProperties) {
        String json = domain.getJson();
        FeedMetadata feedMetadata = ObjectMapperSerializer.deserialize(json, FeedMetadata.class);
        
        populate(feedMetadata, domain);
        
        if (clearSensitiveProperties) {
            clearSensitivePropertyValues(feedMetadata);
        }
        return feedMetadata;
    }

    /**
     * @param feedMetadata
     * @param domain
     */
    private void populate(FeedMetadata feedMetadata, Feed domain) {
    }

    public FeedMetadata deserializeFeedMetadata(Feed domain) {
        return deserializeFeedMetadata(domain, true);
    }

    /**
     * Set the FeedMetadata.registeredTemplate with the template data
     * @param domain the domain JcrFeed
     * @param feed the FeedMetata REST object
     */
    public void setFeedMetadataRegisteredTemplate(@Nonnull final Feed domain,FeedMetadata feed){
        FeedManagerTemplate template = domain.getTemplate();
        if (template != null) {
            RegisteredTemplate registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            feed.setRegisteredTemplate(registeredTemplate);
            feed.setTemplateId(registeredTemplate.getId());
            feed.setTemplateName(registeredTemplate.getTemplateName());
        }
    }

    private FeedMetadata domainToFeedMetadata(@Nonnull final Feed domain, @Nullable final Map<Category, Set<UserFieldDescriptor>> userFieldMap){
        return this.domainToFeedMetadata(domain, userFieldMap,null);
    }
    /**
     * Transforms the specified Metadata feed to a Feed Manager feed.
     *
     * @param domain       the Metadata feed
     * @param userFieldMap cache map from category to user-defined fields, or {@code null}
     * @return the Feed Manager feed
     */
    @Nonnull
    private FeedMetadata domainToFeedMetadata(@Nonnull final Feed domain, @Nullable final Map<Category, Set<UserFieldDescriptor>> userFieldMap, @Nullable ActionGroup actionGroup) {

        FeedMetadata feed = deserializeFeedMetadata(domain, false);
        feed.setId(domain.getId().toString());
        feed.setFeedId(domain.getId().toString());
        feed.setFeedName(domain.getDisplayName());
        feed.setSystemFeedName(domain.getName());
        feed.setDescription(domain.getDescription());
        feed.setAllowIndexing(domain.isAllowIndexing());
        feed.setHistoryReindexingStatus(domain.getCurrentHistoryReindexingStatus().getHistoryReindexingState().toString());
        feed.setOwner(domain.getOwner() != null ? new User(domain.getOwner().getName()) : null);
        
        if (domain.getCreatedTime() != null) {
            feed.setCreateDate(domain.getCreatedTime().toDate());
        }
        if (domain.getModifiedTime() != null) {
            feed.setUpdateDate(domain.getModifiedTime().toDate());
        }

        setFeedMetadataRegisteredTemplate(domain,feed);

        Category category = domain.getCategory();
        if (category != null) {
            feed.setCategory(categoryModelTransform.domainToFeedCategorySimple(category));
        }
        feed.setState(domain.getState() != null ? domain.getState().name() : null);

        // Set user-defined properties
        final Set<UserFieldDescriptor> userFields;
        if (userFieldMap == null) {
            userFields = getUserFields(category);
        } else if (userFieldMap.containsKey(category)) {
            userFields = userFieldMap.get(category);
        } else {
            userFields = getUserFields(category);
            userFieldMap.put(category, userFields);
        }

        @SuppressWarnings("unchecked") final Set<UserProperty> userProperties = UserPropertyTransform.toUserProperties(domain.getUserProperties(), userFields);
        feed.setUserProperties(userProperties);

        // Convert JCR securitygroup to DTO
        List<com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup> restSecurityGroups = new ArrayList<>();
        if (domain.getSecurityGroups() != null && domain.getSecurityGroups().size() > 0) {
            for (Object group : domain.getSecurityGroups()) {
                HadoopSecurityGroup hadoopSecurityGroup = (HadoopSecurityGroup) group;
                com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup restSecurityGroup = new com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup();
                restSecurityGroup.setDescription(hadoopSecurityGroup.getDescription());
                restSecurityGroup.setId(hadoopSecurityGroup.getGroupId());
                restSecurityGroup.setName(hadoopSecurityGroup.getName());
                restSecurityGroups.add(restSecurityGroup);
            }
        }
        feed.setSecurityGroups(restSecurityGroups);
        
        feed.setTags(domain.getTags().stream().map(name -> new DefaultTag(name)).collect(Collectors.toList()));

        if (domain.getUsedByFeeds() != null) {
            final List<FeedSummary> usedByFeeds = domain.getUsedByFeeds().stream()
                .map(this::domainToFeedSummary)
                .collect(Collectors.toList());
            feed.setUsedByFeeds(usedByFeeds);
        }
        
        List<DataSet> srcDataSets = domain.getSources().stream()
            .map(FeedConnection::getDataSet)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(dataSet -> {
                try {
                    return catalogModelTransform.dataSetToRestModel().apply(dataSet);
                } catch (final AccessControlException e) {
                    log.debug("Denied access to data set: {}: {}", domain.getId(), e);

                    final DataSource dataSource = new DataSource();
                    dataSource.setAllowedActions(new ActionGroup());
                    dataSource.getAllowedActions().setActions(Collections.emptyList());

                    final DataSet model = new DataSet();
                    model.setId(domain.getId().toString());
                    model.setDataSource(dataSource);
                    return model;
                }
            })
            .collect(Collectors.toList());
        feed.setSourceDataSets(srcDataSets);

        if(actionGroup == null) {
            //add in access control items
            securityTransform.applyAccessControl(domain, feed);
        }
        else {
            feed.setAllowedActions(actionGroup);
        }

        return feed;
    }

    /**
     * Transforms the specified Metadata feed to a Feed Manager feed summary.
     *
     * @param feedManagerFeed the Metadata feed
     * @return the Feed Manager feed summary
     */
    public FeedSummary domainToFeedSummary(@Nonnull final Feed feedManagerFeed) {
        Category category = feedManagerFeed.getCategory();
        if (category == null) {
            return null;
        }

        FeedSummary feedSummary = new FeedSummary();
        feedSummary.setId(feedManagerFeed.getId().toString());
        feedSummary.setFeedId(feedManagerFeed.getId().toString());

        feedSummary.setCategoryId(category.getId().toString());
        if (category instanceof Category) {
            feedSummary.setCategoryIcon(category.getIcon());
            feedSummary.setCategoryIconColor(category.getIconColor());
        }
        feedSummary.setCategoryName(category.getDisplayName());
        feedSummary.setSystemCategoryName(category.getSystemName());
        feedSummary.setUpdateDate(feedManagerFeed.getModifiedTime() != null ? feedManagerFeed.getModifiedTime().toDate() : null);
        feedSummary.setFeedName(feedManagerFeed.getDisplayName());
        feedSummary.setSystemFeedName(feedManagerFeed.getName());
        feedSummary.setActive(feedManagerFeed.getState() != null && feedManagerFeed.getState().equals(Feed.State.ENABLED));

        feedSummary.setState(feedManagerFeed.getState() != null ? feedManagerFeed.getState().name() : null);

        if (feedManagerFeed instanceof Feed) {

            Feed fmf = (Feed) feedManagerFeed;
            if (fmf.getTemplate() != null) {
                feedSummary.setTemplateId(fmf.getTemplate().getId().toString());
                feedSummary.setTemplateName(fmf.getTemplate().getName());
            }
        }
        //add in access control items
        securityTransform.applyAccessControl(feedManagerFeed, feedSummary);

        return feedSummary;

    }

    /**
     * Transforms the specified Metadata feeds to Feed Manager feed summaries.
     *
     * @param domain the Metadata feed
     * @return the Feed Manager feed summaries
     */
    @Nonnull
    public List<FeedSummary> domainToFeedSummary(@Nonnull final Collection<? extends Feed> domain) {
        return domain.stream().map(this::domainToFeedSummary).filter(feedSummary -> feedSummary != null).collect(Collectors.toList());
    }
    
    /**
     * Transforms the specified Metadata feeds to Feed Manager feed summaries.
     *
     * @param domain the Metadata feed
     * @return the Feed Manager feed summaries
     */
    @Nonnull
    public Page<UIFeed> domainToFeedSummary(@Nonnull final Page<Feed> domain, @Nonnull final Pageable pageable) {
        List<UIFeed> summaries = domain.getContent().stream()
                        .map(this::domainToFeedSummary)
                        .filter(feedSummary -> feedSummary != null)
                        .collect(Collectors.toList());
        return new PageImpl<>(summaries, pageable, domain.getTotalElements());
    }

    /**
     * Gets the user-defined fields including those for the specified category.
     *
     * @param category the domain category
     * @return the user-defined fields
     */
    @Nonnull
    public Set<UserFieldDescriptor> getUserFields(@Nullable final Category category) {
        final Set<UserFieldDescriptor> userFields = feedProvider.getUserFields();
        return (category != null) ? Sets.union(userFields, categoryProvider.getFeedUserFields(category.getId()).orElse(Collections.emptySet())) : userFields;
    }

    /**
     * @param fromVer
     * @param toVer
     */
    public EntityVersionDifference generateDifference(com.thinkbiganalytics.feedmgr.rest.model.EntityVersion fromVer, 
                                                      com.thinkbiganalytics.feedmgr.rest.model.EntityVersion toVer) {
        try {
            ObjectMapper om = new ObjectMapper();
            ObjectWriter ow = om.writer();
            ObjectReader or = om.reader();
            String fromEntStr = ow.writeValueAsString(fromVer.getEntity());
            String toEntStr = ow.writeValueAsString(toVer.getEntity());
            JsonNode fromNode = or.readTree(fromEntStr);
            JsonNode toNode = or.readTree(toEntStr);
            // Produce a patch showing the changes from the "to" node back into the "from" node.
            // This is because we will be providing the "to" entity content so the patch should show the original "from" values.
            JsonNode diff = JsonDiff.asJson(toNode, fromNode);
            com.thinkbiganalytics.feedmgr.rest.model.EntityVersion fromNoContent 
                = new com.thinkbiganalytics.feedmgr.rest.model.EntityVersion(fromVer.getId(), 
                                                                             fromVer.getName(), 
                                                                             fromVer.getCreatedDate(), 
                                                                             fromVer.getCreatedBy(), 
                                                                             fromVer.getComment(), 
                                                                             fromVer.getEntityId());
            
            return new EntityVersionDifference(fromVer, toVer, diff);
        } catch (IOException e) {
            throw new ModelTransformException("Failed to generate entity difference between entity versions " + fromVer.getId() + " and " + toVer.getId());
        }
    }

    /**
     * Updates the data sets of the specified feed.
     */
    public void updateDataSets(@Nullable final FeedMetadata feed) {
        if (feed != null && feed.getSourceDataSets() != null) {
            feed.setSourceDataSets(
                feed.getSourceDataSets().stream()
                    .peek(dataSet -> {
                         com.thinkbiganalytics.metadata.api.catalog.DataSource dataSource = null;
                        if(dataSet.getDataSource() != null && dataSet.getDataSource().getId() != null) {
                            final com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dataSourceId = dataSourceProvider.resolveId(dataSet.getDataSource().getId());
                            dataSource = dataSourceProvider.find(dataSourceId).orElse(null);
                        }
                        //add empty datasource if user doesnt have access
                        if (dataSource == null && !accessController.hasPermission(dataSource, DatasourceAccessControl.ACCESS_DATASOURCE, DatasourceAccessControl.ACCESS_DETAILS)) {
                            dataSet.setDataSource(new com.thinkbiganalytics.kylo.catalog.rest.model.DataSource());
                            dataSet.getDataSource().setAllowedActions(new ActionGroup());
                        }
                    })
                    .collect(Collectors.toList())
            );
        }
    }
}
