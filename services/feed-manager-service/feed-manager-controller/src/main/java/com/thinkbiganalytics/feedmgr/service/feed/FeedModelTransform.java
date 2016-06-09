package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by sr186054 on 5/11/16.
 */
public class FeedModelTransform {

    @Inject
    FeedManagerCategoryProvider categoryProvider;


    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private TemplateModelTransform templateModelTransform;

    @Inject
    private CategoryModelTransform categoryModelTransform;


    @Inject
    private HiveService hiveService;

    public void refreshTableSchemaFromHive(FeedMetadata feed) {

        //Merge back in the hive table schema ?
        if (feed.getRegisteredTemplate() != null && (feed.getRegisteredTemplate().isDefineTable() || feed.getRegisteredTemplate().isDataTransformation()) && feed.getTable() != null
            && feed.getTable().getTableSchema() != null) {
            TableSchema existingSchema = feed.getTable().getTableSchema();
            Map<String, Field> existingFields = existingSchema.getFieldsAsMap();
            String hiveTable = feed.getTable().getTableSchema().getName();
            String categoryName = feed.getCategory().getSystemName();
            try {
                TableSchema schema = hiveService.getTableSchema(categoryName, hiveTable);
                //merge back in previous nuillable/pk values for each of the fields
                if (schema != null && schema.getFields() != null) {
                    for (Field field : schema.getFields()) {
                        Field existingField = existingFields.get(field.getName());
                        if (existingField != null) {
                            field.setPrimaryKey(existingField.getPrimaryKey());
                            field.setNullable(existingField.getNullable());
                        }
                    }
                    feed.getTable().setTableSchema(schema);
                }
            } catch (Exception e) {
                //swallow exception.  refreshing of the hive schema is just a nice to have feature. JSON should be up to date.
            }

        }

    }

    public FeedManagerFeed feedToDomain(FeedMetadata feedMetadata) {
        //resolve the id
        boolean isNew = feedMetadata.getId() == null;
        Feed.ID domainId = feedMetadata.getId() != null ? feedManagerFeedProvider.resolveId(feedMetadata.getId()) : null;
        FeedManagerFeed domain = domainId != null ? feedManagerFeedProvider.findById(domainId) : null;

        FeedCategory restCategoryModel = feedMetadata.getCategory();
        Category category = null;

        if (restCategoryModel != null && (domain == null || (domain != null && domain.getCategory() == null))) {
            category = categoryProvider.findById(categoryProvider.resolveId(restCategoryModel.getId()));
        }

        if (domain == null) {
            isNew = true;

            //1 ensure the Category exists
            if (category == null) {
                throw new RuntimeException("Category cannot be found while creating feed " + feedMetadata.getSystemFeedName() + ".  Category Id is " + restCategoryModel.getId());
            }
            domain = feedManagerFeedProvider.ensureFeed(category.getId(), feedMetadata.getSystemFeedName());
            domainId = domain.getId();
            domain.setState(Feed.State.ENABLED);
            //reassign the domain data back to the ui model....
            feedMetadata.setFeedId(domainId.toString());
            feedMetadata.setState(Feed.State.ENABLED.name());

        }
        domain.setDisplayName(feedMetadata.getFeedName());
        domain.setDescription(feedMetadata.getDescription());

        feedMetadata.setId(domain.getId().toString());

        RegisteredTemplate template = feedMetadata.getRegisteredTemplate();
        if (template != null) {
            //TODO is this needed, or should it just be looked up and assigned
            FeedManagerTemplate domainTemplate = templateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(template);
            domain.setTemplate(domainTemplate);
        }
        if (StringUtils.isNotBlank(feedMetadata.getState())) {
            Feed.State state = Feed.State.valueOf(feedMetadata.getState().toUpperCase());
            domain.setState(state);
        }
        domain.setNifiProcessGroupId(feedMetadata.getNifiProcessGroupId());

        domain.setJson(ObjectMapperSerializer.serialize(feedMetadata));
        if (feedMetadata.getVersion() == null) {
            feedMetadata.setVersion(1L);
        }

        //Datasource datasource = NifiFeedDatasourceFactory.transformSources(feedMetadata);
        // feedProvider.ensureFeedSource()

        if (domain.getTemplate() == null) {

            FeedManagerTemplate.ID templateId = templateProvider.resolveId(feedMetadata.getTemplateId());
            FeedManagerTemplate domainTemplate = templateProvider.findById(templateId);
            domain.setTemplate(domainTemplate);
        }

        domain.setVersion(feedMetadata.getVersion().intValue());
        return domain;


    }


    public final Function<FeedManagerFeed, FeedMetadata>
        DOMAIN_TO_FEED =
        new Function<FeedManagerFeed, FeedMetadata>() {
            @Override
            public FeedMetadata apply(FeedManagerFeed domain) {
                String json = domain.getJson();
                FeedMetadata feed = ObjectMapperSerializer.deserialize(json, FeedMetadata.class);
                feed.setId(domain.getId().toString());
                feed.setFeedId(domain.getId().toString());
                feed.setTemplateId(domain.getTemplate().getId().toString());
                if (domain.getCreatedTime() != null) {
                    feed.setCreateDate(domain.getCreatedTime().toDate());
                }
                if (domain.getModifiedTime() != null) {
                    feed.setUpdateDate(domain.getModifiedTime().toDate());
                }

                FeedManagerTemplate template = domain.getTemplate();
                if (template != null) {
                    RegisteredTemplate registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
                    feed.setRegisteredTemplate(registeredTemplate);
                    feed.setTemplateId(registeredTemplate.getId());
                }
                FeedManagerCategory category = domain.getCategory();
                if (category != null) {
                    FeedCategory feedCategory = categoryModelTransform.DOMAIN_TO_FEED_CATEGORY_SIMPLE.apply(category);
                    feed.setCategory(feedCategory);
                }
                feed.setState(domain.getState() != null ? domain.getState().name() : null);
                feed.setVersion(domain.getVersion().longValue());
                return feed;
            }
        };

    public final Function<Feed, FeedSummary> DOMAIN_TO_FEED_SUMMARY = new Function<Feed, FeedSummary>() {
        @Nullable
        @Override
        public FeedSummary apply(@Nullable Feed feedManagerFeed) {
            FeedSummary feedSummary = new FeedSummary();
            feedSummary.setId(feedManagerFeed.getId().toString());
            feedSummary.setFeedId(feedManagerFeed.getId().toString());
            feedSummary.setCategoryId(feedManagerFeed.getCategory().getId().toString());
            feedSummary.setCategoryIcon(((FeedManagerCategory) feedManagerFeed.getCategory()).getIcon());
            feedSummary.setCategoryIconColor(((FeedManagerCategory) feedManagerFeed.getCategory()).getIconColor());
            feedSummary.setCategoryName(feedManagerFeed.getCategory().getDisplayName());
            feedSummary.setSystemCategoryName(feedManagerFeed.getCategory().getName());
            feedSummary.setUpdateDate(feedManagerFeed.getModifiedTime() != null ? feedManagerFeed.getModifiedTime().toDate(): null);
            feedSummary.setFeedName(feedManagerFeed.getDisplayName());
            feedSummary.setSystemFeedName(feedManagerFeed.getName());
            feedSummary.setActive(feedManagerFeed.getState() != null ? feedManagerFeed.getState().equals(Feed.State.ENABLED) : false);
            return feedSummary;
        }
    };


    public List<FeedMetadata> domainToFeedMetadata(Collection<? extends FeedManagerFeed> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED));
    }


    public List<FeedSummary> domainToFeedSummary(Collection<? extends Feed> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_SUMMARY));
    }


}
