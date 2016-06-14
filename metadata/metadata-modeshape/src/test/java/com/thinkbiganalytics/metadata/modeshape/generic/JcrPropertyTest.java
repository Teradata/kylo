package com.thinkbiganalytics.metadata.modeshape.generic;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * Created by sr186054 on 6/4/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class})
public class JcrPropertyTest {

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyTest.class);


    @Inject
    private ExtensibleTypeProvider provider;

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    DatasourceProvider datasourceProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    FeedManagerTemplateProvider feedManagerTemplateProvider;

    @Inject
    FeedManagerCategoryProvider feedManagerCategoryProvider;

    @Inject
    TagProvider tagProvider;

    @Inject
    private MetadataAccess metadata;


    @Test
    public void testGetPropertyTypes() throws RepositoryException {
        Map<String, FieldDescriptor.Type> propertyTypeMap = metadata.commit(new Command<Map<String, FieldDescriptor.Type>>() {
            @Override
            public Map<String, FieldDescriptor.Type> execute() {
                ExtensibleType feedType = provider.getType("tba:feed");
                Set<FieldDescriptor> fields = feedType.getFieldDescriptors();
                Map<String, FieldDescriptor.Type> map = new HashMap<>();
                
                for (FieldDescriptor field : fields) {
                    map.put(field.getName(), field.getType());
                }
                
                return map;
            }
        });
        log.info("Property Types {} ", propertyTypeMap);

    }

    @Test
    public void testFeed() {
        String categorySystemName = "my_category";
        Category category = metadata.commit(new Command<Category>() {
            @Override
            public Category execute() {
                JcrCategory category = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
                category.setDescription("my category desc");
                category.setTitle("my category");
                categoryProvider.update(category);
                return category;
            }
        });


        final JcrFeed feed = metadata.commit(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {

                String categorySystemName = "my_category";

                JcrCategory category = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
                category.setDescription("my category desc");
                category.setTitle("my category");
                categoryProvider.update(category);

                JcrDatasource datasource = (JcrDatasource) datasourceProvider.ensureDatasource("mysql.table", "mysql table source");
                datasource.setProperty(JcrDatasource.TYPE_NAME, "Database");

                String feedSystemName = "my_feed";
                JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(categorySystemName, feedSystemName, " my feed desc", datasource.getId(), null);
                feed.setTitle("my feed");
                feed.addTag("my tag");
                feed.addTag("my second tag");
                feed.addTag("feedTag");

                Map<String, Object> otherProperties = new HashMap<String, Object>();
                otherProperties.put("prop1", "my prop1");
                otherProperties.put("prop2", "my prop2");
                feed.setProperties(otherProperties);

                return feed;

            }
        });

        //read and find feed verisons and ensure props

        JcrFeed readFeed = metadata.read(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {
                JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(feed.getId());
                printVersions(f);

                Map<String, Object> props = f.getAllProperties();
                List<JcrSource> sources = f.getSources();
                if (sources != null) {
                    for (JcrSource source : sources) {
                        Map<String, Object> dataSourceProperties = ((JcrDatasource) source.getDatasource()).getAllProperties();
                    }
                }
                List<JcrObject> taggedObjects = tagProvider.findByTag("feedTag");
                return f;
            }
        });

        //update the feed again

        Feed updateFeed = metadata.commit(new Command<Feed>() {
            @Override
            public Feed execute() {
                JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(feed.getId());
                f.setDescription("My Feed Updated Description");
                ((JcrFeedProvider) feedProvider).update(f);
                return f;
            }
        });

        //read it again and find the versions
        readFeed = metadata.read(new Command<JcrFeed>() {
            @Override
            public JcrFeed execute() {
                JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(feed.getId());
                printVersions(f);
                return f;
            }
        });

        List<Feed> feeds = metadata.read(new Command<List<Feed>>() {
            @Override
            public List<Feed> execute() {

                FeedCriteria criteria = feedProvider.feedCriteria();
                criteria.category("my_category");
                // List<Feed> feedsList = feedProvider.getFeeds(criteria);

                //   return feedsList;
                return null;
            }
        });


    }

    private void printVersions(JcrObject o) {
        List<Version> versions = JcrVersionUtil.getVersions(o.getNode());
        int versionCount = versions.size();
        log.info(" {}. Version count: {}", o.getNodeName(), versionCount);
        for (Version v : versions) {
            try {
                log.info("Version: {}", v.getName());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void queryTest() {

        //final String query = "select e.* from [tba:feed] as e  join [tba:category] c on e.[tba:category].[tba:systemName] = c.[tba:systemName] where  c.[tba:systemName] = $category ";
        final String query = "select e.* from [tba:feed] as e join [tba:category] as c on e.[tba:category] = c.[jcr:uuid]";

        metadata.read(new Command<Object>() {
            @Override
            public Object execute() {

                List<Node> feeds = ((JcrFeedProvider) feedProvider).findNodes(query);
                return feeds;
            }
        });

        metadata.read(new Command<Object>() {
            @Override
            public Object execute() {
                List<FeedManagerCategory> c = feedManagerCategoryProvider.findAll();
                if (c != null)
                {
                    for (FeedManagerCategory cat : c) {
                        JcrFeedManagerCategory jcrFeedManagerCategory = (JcrFeedManagerCategory) cat;
                        List<? extends Feed> categoryFeeds = jcrFeedManagerCategory.getFeeds();
                        if (categoryFeeds != null) {
                            for (Feed feed : categoryFeeds) {
                                log.info("Feed for category {} is {}", cat.getName(), feed.getName());
                            }
                        }

                    }
                }
                return null;
            }
        });

    }

    @Test
    public void testFeedManager() {
        FeedManagerFeed feed = metadata.read(new Command<FeedManagerFeed>() {
            @Override
            public FeedManagerFeed execute() {
                List<FeedManagerFeed> feeds = feedManagerFeedProvider.findAll();
                if (feeds != null) {
                    return feeds.get(0);
                }
                return null;
            }
        });

    }


}






