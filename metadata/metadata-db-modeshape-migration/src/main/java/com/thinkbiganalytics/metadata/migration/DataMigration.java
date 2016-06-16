package com.thinkbiganalytics.metadata.migration;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 6/15/16.
 */
public class DataMigration {


    private Map<Category.ID, Category.ID> databaseCategoryToJcrMap = new HashMap<>();
    private Map<Category.ID, Category.ID> jcrCategoryToDatabaseMap = new HashMap<>();
    List<JcrFeedManagerCategory> categories = new ArrayList<JcrFeedManagerCategory>();


    private Map<FeedManagerTemplate.ID, FeedManagerTemplate.ID> databaseTemplateToJcrMap = new HashMap<>();
    private Map<FeedManagerTemplate.ID, FeedManagerTemplate.ID> jcrTemplateToDatabaseMap = new HashMap<>();
    List<JcrFeedTemplate> templates = new ArrayList<JcrFeedTemplate>();


    private Map<Feed.ID, Feed.ID> databaseFeedToJcrMap = new HashMap<>();
    private Map<Feed.ID, Feed.ID> jcrFeedToDatabaseMap = new HashMap<>();
    List<JcrFeedManagerFeed> feeds = new ArrayList<JcrFeedManagerFeed>();


    public void addJcrCategory(JcrFeedManagerCategory jcrCategory, Category.ID databaseId) {
        databaseCategoryToJcrMap.put(databaseId, jcrCategory.getId());
        jcrCategoryToDatabaseMap.put(jcrCategory.getId(), databaseId);
        categories.add(jcrCategory);
    }

    public void addJcrTemplate(JcrFeedTemplate template, FeedManagerTemplate.ID databaseId) {
        databaseTemplateToJcrMap.put(databaseId, template.getId());
        jcrTemplateToDatabaseMap.put(template.getId(), databaseId);
        templates.add(template);
    }

    public void addJcrFeed(JcrFeedManagerFeed feed, Feed.ID databaseId) {
        databaseFeedToJcrMap.put(databaseId, feed.getId());
        jcrFeedToDatabaseMap.put(feed.getId(), databaseId);
        feeds.add(feed);
    }


    public Category.ID getJcrCategory(Category.ID databaseCategoryId) {
        return databaseCategoryToJcrMap.get(databaseCategoryId);
    }

    public FeedManagerTemplate.ID getJcrTemplateId(FeedManagerTemplate.ID databaseId) {
        return databaseTemplateToJcrMap.get(databaseId);
    }

    public JcrFeedTemplate getJcrTemplateForDatabaseId(FeedManagerTemplate.ID databaseId) {
        FeedManagerTemplate.ID jcrTemplateId = getJcrTemplateId(databaseId);
        if (jcrTemplateId != null) {
            JcrFeedTemplate template = Iterables.tryFind(templates, new Predicate<JcrFeedTemplate>() {
                @Override
                public boolean apply(JcrFeedTemplate jcrFeedTemplate) {
                    return jcrTemplateId.equals(jcrFeedTemplate.getId());
                }
            }).orNull();
            return template;
        }
        return null;
    }

    public void clear() {
        databaseCategoryToJcrMap.clear();
        jcrCategoryToDatabaseMap.clear();
        categories.clear();
        databaseTemplateToJcrMap.clear();
        jcrTemplateToDatabaseMap.clear();
        templates.clear();

        databaseFeedToJcrMap.clear();
        jcrFeedToDatabaseMap.clear();
        feeds.clear();

    }

    public Statistics getStatistics() {
        Statistics stats = new Statistics();
        stats.setDatabaseCategoryToJcrMap(databaseCategoryToJcrMap);
        stats.setDatabaseFeedToJcrMap(databaseFeedToJcrMap);
        stats.setDatabaseTemplateToJcrMap(databaseTemplateToJcrMap);
        return stats;
    }

    public class Statistics {

        private Map<Category.ID, Category.ID> databaseCategoryToJcrMap = new HashMap<>();
        private Map<FeedManagerTemplate.ID, FeedManagerTemplate.ID> databaseTemplateToJcrMap = new HashMap<>();
        private Map<Feed.ID, Feed.ID> databaseFeedToJcrMap = new HashMap<>();

        private Integer templatesMigrated = 0;
        private Integer categoriesMigrated = 0;
        private Integer feedsMigrated = 0;

        public Map<Category.ID, Category.ID> getDatabaseCategoryToJcrMap() {
            return databaseCategoryToJcrMap;
        }

        public void setDatabaseCategoryToJcrMap(Map<Category.ID, Category.ID> databaseCategoryToJcrMap) {
            this.databaseCategoryToJcrMap = databaseCategoryToJcrMap;
            this.categoriesMigrated = this.databaseCategoryToJcrMap.size();
        }

        public Map<FeedManagerTemplate.ID, FeedManagerTemplate.ID> getDatabaseTemplateToJcrMap() {
            return databaseTemplateToJcrMap;
        }

        public void setDatabaseTemplateToJcrMap(
            Map<FeedManagerTemplate.ID, FeedManagerTemplate.ID> databaseTemplateToJcrMap) {
            this.databaseTemplateToJcrMap = databaseTemplateToJcrMap;
            this.templatesMigrated = this.databaseTemplateToJcrMap.size();
        }

        public Map<Feed.ID, Feed.ID> getDatabaseFeedToJcrMap() {
            return databaseFeedToJcrMap;
        }

        public void setDatabaseFeedToJcrMap(Map<Feed.ID, Feed.ID> databaseFeedToJcrMap) {
            this.databaseFeedToJcrMap = databaseFeedToJcrMap;
            this.feedsMigrated = this.databaseFeedToJcrMap.size();
        }


        public Integer getTemplatesMigrated() {
            return templatesMigrated;
        }

        public Integer getCategoriesMigrated() {
            return categoriesMigrated;
        }

        public Integer getFeedsMigrated() {
            return feedsMigrated;
        }
    }


}
