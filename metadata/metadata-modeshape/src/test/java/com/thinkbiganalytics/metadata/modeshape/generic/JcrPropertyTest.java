package com.thinkbiganalytics.metadata.modeshape.generic;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryPovider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/4/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class})
public class JcrPropertyTest {

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyTest.class);


    @Inject
    private GenericEntityProvider provider;

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    private MetadataAccess metadata;


    @Test
    public void testGetPropertyTypes() throws RepositoryException {
        Map<String, GenericType.PropertyType> propertyTypeMap = metadata.commit(new Command<Map<String, GenericType.PropertyType>>() {
            @Override
            public Map<String, GenericType.PropertyType> execute() {
                Map<String, GenericType.PropertyType> m = ((JcrGenericEntityProvider) provider).getPropertyTypes("tba:feed");
                return m;
            }
        });
        log.info("Property Types {} ", propertyTypeMap);

    }

    @Test
    public void testFeed() {
        final JcrFeed feed = metadata.commit(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {

                Map<String, Object> props = new HashMap<String, Object>();
                props.put(JcrCategory.TITLE, "my category");
                props.put(JcrCategory.SYSTEM_NAME, "my_category");
                props.put(JcrCategory.DESCRIPTION, "my cat desc");

                Category category = ((JcrCategoryPovider) categoryProvider).createCategory(props);

                props = new HashMap<String, Object>();
                props.put(JcrCategory.TITLE, "my feed");
                props.put(JcrCategory.SYSTEM_NAME, "my_feed");
                props.put(JcrCategory.DESCRIPTION, "my feed desc");

                JcrFeed feed = ((JcrFeedProvider) feedProvider).createFeed(category.getId(), props);

                Map<String, Object> otherProperties = new HashMap<String, Object>();
                otherProperties.put("prop1", "my prop1");
                feed.setProperties(otherProperties);

                category.getFeeds();

                return feed;

            }
        });

        JcrFeed readFeed = metadata.read(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {
                JcrFeed f = ((JcrFeedProvider) feedProvider).findById(feed.getId());
                List<Category> categoryList = categoryProvider.findAll();
                return f;
            }
        });


    }
}



