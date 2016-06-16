package com.thinkbiganalytics.metadata.migration;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.migration.category.CategoryDatabaseProvider;
import com.thinkbiganalytics.metadata.migration.feed.FeedDatabaseProvider;
import com.thinkbiganalytics.metadata.migration.feed.FeedManagerFeedDTO;
import com.thinkbiganalytics.metadata.migration.template.TemplateDatabaseProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

import java.util.Map;

import javax.inject.Inject;

/**
 * Created by sr186054 on 6/15/16.
 */
public class FeedMigrationService {


    @Inject
    FeedDatabaseProvider feedDatabaseProvider;

    @Inject
    CategoryDatabaseProvider categoryDatabaseProvider;

    @Inject
    TemplateDatabaseProvider templateDatabaseProvider;

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    FeedManagerFeedProvider feedProvider;


    @Inject
    MetadataAccess metadataAccess;

    private DataMigration dataMigration;

    private Map<FeedManagerTemplate.ID, FeedManagerTemplate> databaseTemplates;
    private Map<Category.ID, FeedManagerCategory> databaseCategories;
    private Map<Feed.ID, FeedManagerFeed> databaseFeeds;


    private JcrFeedManagerCategory createJcrCategory(FeedManagerCategory databaseCategory) {
        JcrFeedManagerCategory category = (JcrFeedManagerCategory) categoryProvider.ensureCategory(databaseCategory.getName());
        category.setDescription(databaseCategory.getDescription());
        category.setDisplayName(databaseCategory.getDisplayName());
        category.setDescription(databaseCategory.getDescription());
        category.setIcon(databaseCategory.getIcon());
        category.setIconColor(databaseCategory.getIconColor());
        category.setCreatedTime(databaseCategory.getCreatedTime());
        category.setModifiedTime(databaseCategory.getModifiedTime());
        categoryProvider.update(category);
        return category;
    }

    private JcrFeedTemplate createJcrTemplate(FeedManagerTemplate databaseTemplate) {
        JcrFeedTemplate template = (JcrFeedTemplate) templateProvider.ensureTemplate(databaseTemplate.getName());
        template.setDescription(databaseTemplate.getDescription());
        template.setNifiTemplateId(databaseTemplate.getNifiTemplateId());
        template.setDefineTable(databaseTemplate.isDefineTable());
        template.setAllowPreconditions(databaseTemplate.isAllowPreconditions());
        template.setDataTransformation(databaseTemplate.isDataTransformation());
        template.setJson(databaseTemplate.getJson());
        template.setModifiedTime(databaseTemplate.getModifiedTime());
        template.setCreatedTime(databaseTemplate.getCreatedTime());
        template.setIcon(databaseTemplate.getIcon());
        template.setIconColor(databaseTemplate.getIconColor());
        return template;
    }

    private JcrFeedManagerFeed createJcrFeed(FeedManagerFeedDTO databaseFeed) {
        Category.ID jcrCategoryId = dataMigration.getJcrCategory(databaseFeed.getCategoryId());
        JcrFeedManagerFeed feed = (JcrFeedManagerFeed) feedProvider.ensureFeed(jcrCategoryId, databaseFeed.getName());

        feed.setDescription(databaseFeed.getDescription());
        feed.setNifiProcessGroupId(databaseFeed.getNifiProcessGroupId());
        feed.setState(databaseFeed.getState());
        feed.setDisplayName(databaseFeed.getDisplayName());
        feed.setJson(databaseFeed.getJson());
        feed.setCreatedTime(databaseFeed.getCreatedTime());
        feed.setModifiedTime(databaseFeed.getModifiedTime());
        feed.setTemplate(dataMigration.getJcrTemplateForDatabaseId(databaseFeed.getTemplateId()));

        return feed;
    }


    private void migrateCategories() {

        databaseCategories = categoryDatabaseProvider.queryCategoriesAsMap();
        if (databaseCategories != null && !databaseCategories.isEmpty()) {

            //1 create the categories
            metadataAccess.commit(new Command<Object>() {
                @Override
                public Object execute() {

                    for (FeedManagerCategory c : databaseCategories.values()) {
                        JcrFeedManagerCategory jcrFeedManagerCategory = createJcrCategory(c);
                        dataMigration.addJcrCategory(jcrFeedManagerCategory, c.getId());
                    }

                    return null;
                }
            });
        }

    }

    private void migrateTemplates() {

        databaseTemplates = templateDatabaseProvider.queryTemplatesAsMap();
        if (databaseTemplates != null && !databaseTemplates.isEmpty()) {
            //1 create the categories
            metadataAccess.commit(new Command<Object>() {
                @Override
                public Object execute() {
                    for (FeedManagerTemplate t : databaseTemplates.values()) {
                        JcrFeedTemplate jcrTemplate = createJcrTemplate(t);
                        dataMigration.addJcrTemplate(jcrTemplate, t.getId());
                    }

                    return null;
                }
            });
        }
    }

    private void migrateFeeds() {

        databaseFeeds = feedDatabaseProvider.queryFeedsAsMap();
        if (databaseFeeds != null && !databaseFeeds.isEmpty()) {
            //1 create the categories
            metadataAccess.commit(new Command<Object>() {
                @Override
                public Object execute() {
                    for (FeedManagerFeed t : databaseFeeds.values()) {
                        JcrFeedManagerFeed jcrFeed = createJcrFeed((FeedManagerFeedDTO) t);
                        dataMigration.addJcrFeed(jcrFeed, t.getId());
                    }

                    return null;
                }
            });
        }
    }

    public void migrate() {
        //LOCK
        dataMigration = new DataMigration();
        migrateCategories();
        migrateTemplates();
        migrateFeeds();

    }


}
