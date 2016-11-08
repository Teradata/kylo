package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.TemplateDeletionException;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 11/7/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics.metadata.modeshape"})
public class TestFeedManagerFeed {

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    FeedManagerTemplateProvider feedManagerTemplateProvider;

    @Inject
    FeedManagerCategoryProvider feedManagerCategoryProvider;

    @Inject
    private JcrMetadataAccess metadata;


    /**
     * returns a FeedManagerTemplate. Must be called within a metadata.commit() call
     */
    private FeedManagerTemplate findOrCreateTemplate(String templateName) {
        FeedManagerTemplate template = feedManagerTemplateProvider.findByName(templateName);
        if (template == null) {
            template = feedManagerTemplateProvider.ensureTemplate(templateName);
            return template;
        } else {
            return template;
        }
    }

    /**
     * must be called within metdata.commit()
     */
    private Category findOrCreateCategory(String categorySystemName) {
        Category category = categoryProvider.findBySystemName(categorySystemName);
        if (category == null) {
            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
            cat.setDescription(categorySystemName + " desc");
            cat.setTitle(categorySystemName);
            categoryProvider.update(cat);
            category = cat;
        }
        return category;
    }

    private FeedManagerFeed findOrCreateFeed(String categorySystemName, String feedSystemName, String feedTemplate) {
        Category category = findOrCreateCategory(categorySystemName);
        FeedManagerFeed feed = feedManagerFeedProvider.ensureFeed(category.getId(), feedSystemName);
        feed.setDisplayName(feedSystemName);
        FeedManagerTemplate template = findOrCreateTemplate(feedTemplate);
        feed.setTemplate(template);
        return feedManagerFeedProvider.update(feed);
    }

    private FeedManagerFeed findFeed(String categorySystemName, String feedSystemName) {
        FeedManagerFeed feed = feedManagerFeedProvider.findBySystemName(categorySystemName, feedSystemName);
        return feed;
    }

    private boolean deleteTemplate(String templateName) {
        //try to delete the template.  This should fail since there are feeds attached to it
        return metadata.commit(new AdminCredentials(), () -> {
            FeedManagerTemplate template = findOrCreateTemplate(templateName);
            return feedManagerTemplateProvider.deleteTemplate(template.getId());

        });
    }


    @Test
    public void testFeedTemplates() {
        String categorySystemName = "my_category";
        String feedName = "my_feed";
        String templateName = "my_template";

        //first create the category
        metadata.commit(new AdminCredentials(), () -> {
            Category category = findOrCreateCategory(categorySystemName);
            return category.getId();
        });

        //creqte the feed
        metadata.commit(new AdminCredentials(), () -> {
            FeedManagerFeed feed = findOrCreateFeed(categorySystemName, feedName, templateName);
            return feed.getId();
        });

        //ensure the feed relates to the template
        metadata.read(new AdminCredentials(), () -> {
            FeedManagerTemplate template = findOrCreateTemplate(templateName);
            List<FeedManagerFeed> feeds = template.getFeeds();
            Assert.assertTrue(feeds != null && feeds.size() > 0);
        });

        //try to delete the template.  This should fail since there are feeds attached to it
        Boolean deleteStatus = null;
        try {
            deleteStatus = deleteTemplate(templateName);
        } catch (TemplateDeletionException e) {
            Assert.assertNotNull(e);
            deleteStatus = false;
        }
        //assert that we could not delete it
        Assert.assertFalse(deleteStatus);

        //try to delete the feed
        metadata.commit(new AdminCredentials(), () -> {
            FeedManagerFeed feed = findFeed(categorySystemName, feedName);
            Assert.assertNotNull(feed);
            if (feed != null) {
                feedManagerFeedProvider.delete(feed);
            }
        });

        //ensure it is deleted
        metadata.read(new AdminCredentials(), () -> {
            FeedManagerFeed feed = findFeed(categorySystemName, feedName);
            Assert.assertNull(feed);
        });

        //try to delete the template.  This should succeed since the feeds are gone
        deleteStatus = deleteTemplate(templateName);
        Assert.assertEquals(deleteStatus.booleanValue(), true);


    }


}
