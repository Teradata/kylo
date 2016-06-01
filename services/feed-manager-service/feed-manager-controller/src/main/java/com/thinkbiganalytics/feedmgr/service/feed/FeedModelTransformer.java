package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategory;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.feed.JpaFeedManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/11/16.
 */
public class FeedModelTransformer {

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private HiveService hiveService;

    public void refreshTableSchemaFromHive(FeedMetadata feed) {

        //Merge back in the hive table schema ?
        if (feed.getTable() != null && feed.getTable().getTableSchema() != null) {
            TableSchema existingSchema = feed.getTable().getTableSchema();
            Map<String, Field> existingFields = existingSchema.getFieldsAsMap();
            String hiveTable = feed.getTable().getTableSchema().getName();
            String categoryName = feed.getCategory().getSystemName();
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
        }

    }

    public FeedManagerFeed feedToDomain(FeedMetadata feedMetadata) {
        //resolve the id
        boolean isNew = feedMetadata.getId() == null;
        JpaFeed.FeedId domainId = feedMetadata.getId() != null ? new JpaFeed.FeedId(feedMetadata.getId()) : JpaFeed.FeedId.create();
        JpaFeedManagerFeed domain = null;
        domain = (JpaFeedManagerFeed) feedManagerFeedProvider.findById(domainId);
        if (domain == null) {
            isNew = true;
            domain = new JpaFeedManagerFeed(domainId, feedMetadata.getSystemFeedName(), feedMetadata.getDescription());
            domain.setState(Feed.State.ENABLED);
            feedMetadata.setId(domainId.toString());
            feedMetadata.setFeedId(domainId.toString());
            feedMetadata.setState(Feed.State.ENABLED.name());
        }
        domain.setDisplayName(feedMetadata.getFeedName());
        domain.setDescription(feedMetadata.getDescription());

        feedMetadata.setId(domain.getId().toString());

        FeedCategory category = feedMetadata.getCategory();
        if (category != null && domain.getCategory() == null) {
            JpaFeedManagerCategory domainCategory = (JpaFeedManagerCategory) categoryProvider.findById(new JpaCategory.CategoryId(category.getId()));
            domain.setCategory(domainCategory);
        }
        RegisteredTemplate template = feedMetadata.getRegisteredTemplate();
        if (template != null) {
            //TODO is this needed, or should it just be looked up and assigned
            FeedManagerTemplate domainTemplate = TemplateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(template);
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
            FeedManagerTemplate.ID templateId = new JpaFeedManagerTemplate.FeedManagerTemplateId(feedMetadata.getTemplateId());
            FeedManagerTemplate domainTemplate = templateProvider.findById(templateId);
            domain.setTemplate(domainTemplate);
        }

        domain.setVersion(feedMetadata.getVersion().intValue());
        return domain;


    }


}
