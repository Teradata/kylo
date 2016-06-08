package com.thinkbiganalytics.metadata.modeshape.generic;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
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
    DatasourceProvider datasourceProvider;

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

                String categorySystemName = "my_category";

                JcrCategory category = (JcrCategory)categoryProvider.ensureCategory(categorySystemName);
                category.setDescription("my category desc");
                category.setTitle("my category");
                String feedSystemName = "my_feed";

                JcrDatasource datasource = (JcrDatasource) datasourceProvider.ensureDatasource("mysql.table", "mysql table source");
                datasource.setProperty(JcrDatasource.TYPE_NAME, "Database");

                JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(categorySystemName, feedSystemName, " my feed desc", datasource.getId(), null);
                feed.setTitle("my feed");

                Map<String, Object> otherProperties = new HashMap<String, Object>();
                otherProperties.put("prop1", "my prop1");
                otherProperties.put("prop2", "my prop2");
                feed.setProperties(otherProperties);

                return feed;

            }
        });

        JcrFeed readFeed = metadata.read(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {
                JcrFeed f = ((JcrFeedProvider) feedProvider).findById(feed.getId());
                List<JcrSource> sources = f.getSources();
                if (sources != null) {
                    for (JcrSource source : sources) {
                        Map<String, Object> dataSourceProperties = ((JcrDatasource) source.getDatasource()).getAllProperties();
                    }
                }
                List<Category> categoryList = categoryProvider.findAll();
                return f;
            }
        });


    }
}



