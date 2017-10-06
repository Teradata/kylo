package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDerivedDatasource;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, JcrPropertyTestConfig.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics.metadata.modeshape.op"})
public class JcrPropertyTest {

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyTest.class);
    @Inject
    CategoryProvider categoryProvider;
    @Inject
    DatasourceProvider datasourceProvider;
    @Inject
    FeedProvider feedProvider;
    @Inject
    TagProvider tagProvider;
    @Inject
    private JcrExtensibleTypeProvider provider;
    @Inject
    private JcrMetadataAccess metadata;

    @Test
    public void testGetPropertyTypes() throws RepositoryException {
        Map<String, FieldDescriptor.Type> propertyTypeMap = metadata.commit(() -> {
            provider.ensureTypeDescriptors();
            
            ExtensibleType feedType = provider.getType("tba:feed");
            Set<FieldDescriptor> fields = feedType.getFieldDescriptors();
            Map<String, FieldDescriptor.Type> map = new HashMap<>();

            for (FieldDescriptor field : fields) {
                map.put(field.getName(), field.getType());
            }

            return map;
        }, MetadataAccess.SERVICE);
        log.info("Property Types {} ", propertyTypeMap);

    }


    /**
     * Creates a new Category Creates a new Feed Updates the Feed Get Feed and its versions Validates Feed is versioned along with its properties and can successfully return a version
     */
    @Test
    public void testFeed() {
        String categorySystemName = "my_category";
        Category category = metadata.commit(() -> {
            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
            cat.setDescription("my category desc");
            cat.setTitle("my category");
            categoryProvider.update(cat);
            return cat;
        }, MetadataAccess.SERVICE);

        final JcrFeed.FeedId createdFeedId = metadata.commit(() -> {

            String sysName = "my_category";

            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(sysName);
            cat.setDescription("my category desc");
            cat.setTitle("my category");
            categoryProvider.update(cat);

            JcrDerivedDatasource datasource1 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HiveDatasource", "mysql.table1", "mysql.table1", "mysql table source 1", null);
            JcrDerivedDatasource datasource2 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HiveDatasource", "mysql.table2", "mysql.table2", "mysql table source 2", null);

            JcrDerivedDatasource emptySource1 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", "", "", "empty hdfs source", null);
            JcrDerivedDatasource emptySource2 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", "", "", "empty hdfs source", null);
            Assert.assertEquals(emptySource1.getId(), emptySource2.getId());
            JcrDerivedDatasource emptySource3 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", null, null, "empty hdfs source", null);
            JcrDerivedDatasource emptySource4 = (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", null, null, "empty hdfs source", null);
            Assert.assertEquals(emptySource3.getId(), emptySource4.getId());
            JcrDerivedDatasource
                datasource3 =
                (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", "/etl/customers/swert/012320342", "/etl/customers/swert/012320342", "mysql hdfs source", null);
            JcrDerivedDatasource
                datasource4 =
                (JcrDerivedDatasource) datasourceProvider.ensureDerivedDatasource("HDFSDatasource", "/etl/customers/swert/012320342", "/etl/customers/swert/012320342", "mysql hdfs source", null);
            Assert.assertEquals(datasource3.getId(), datasource4.getId());

            String feedSystemName = "my_feed_" + UUID.randomUUID();
//                JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(categorySystemName, feedSystemName, "my feed desc", datasource1.getId(), null);
            JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(sysName, feedSystemName, "my feed desc");
            feedProvider.ensureFeedSource(feed.getId(), datasource1.getId());
            feedProvider.ensureFeedSource(feed.getId(), datasource2.getId());
            feed.setTitle("my feed");
            feed.addTag("my tag");
            feed.addTag("my second tag");
            feed.addTag("feedTag");

            Map<String, Object> otherProperties = new HashMap<String, Object>();
            otherProperties.put("prop1", "my prop1");
            otherProperties.put("prop2", "my prop2");
            feed.setProperties(otherProperties);

            return feed.getId();
        }, MetadataAccess.SERVICE);

        //read and find feed verisons and ensure props

        JcrFeed.FeedId readFeedId = metadata.read(() -> {
            Session s = null;
            JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(createdFeedId);
            // TODO: Feed vesioning disabled for Kylo v0.5.0
//            int versions = printVersions(f);
//            Assert.assertTrue(versions > 1, "Expecting more than 1 version: jcr:rootVersion, 1.0");

            @SuppressWarnings("unchecked")
            List<? extends FeedSource> sources = f.getSources();

            Assert.assertTrue(sources.size() > 0);

            if (sources != null) {
                for (FeedSource source : sources) {
                    Map<String, Object> dataSourceProperties = ((JcrDatasource) source.getDatasource()).getAllProperties();
                    String type = (String) dataSourceProperties.get(JcrDerivedDatasource.TYPE_NAME);
                    Assert.assertEquals(type, "HiveDatasource");
                }
            }
            List<JcrObject> taggedObjects = tagProvider.findByTag("my tag");
            //assert we got 1 feed back
            Assert.assertTrue(taggedObjects.size() >= 1);
            return f.getId();
        }, MetadataAccess.SERVICE);

        //update the feed again

        JcrFeed.FeedId updatedFeed = metadata.commit(() -> {
            JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(createdFeedId);
            f.setDescription("My Feed Updated Description");

            Map<String, Object> otherProperties = new HashMap<String, Object>();
            otherProperties.put("prop1", "my updated prop1");
            f.setProperties(otherProperties);

            ((JcrFeedProvider) feedProvider).update(f);
            return f.getId();
        }, MetadataAccess.SERVICE);

        //read it again and find the versions
        readFeedId = metadata.read(() -> {
            JcrFeed f = (JcrFeed) ((JcrFeedProvider) feedProvider).findById(updatedFeed);
            // TODO: Feed vesioning disabled for Kylo v0.5.0
//            int versions = printVersions(f);
//            Assert.assertTrue(versions > 2, "Expecting more than 2 versions: jcr:rootVersion, 1.0, 1.1");
//            JcrFeed v1 = JcrVersionUtil.getVersionedNode(f, "1.0", JcrFeed.class);
//            JcrFeed v11 = JcrVersionUtil.getVersionedNode(f, "1.1", JcrFeed.class);
//            String v1Prop1 = v1.getProperty("prop1", String.class);
//            String v11Prop1 = v11.getProperty("prop1", String.class);
//            JcrFeed baseVersion = JcrVersionUtil.getVersionedNode(JcrVersionUtil.getBaseVersion(f.getNode()), JcrFeed.class);

            //Assert the Props get versioned

//            Assert.assertEquals(v1Prop1, "my prop1");
//            Assert.assertEquals(v11Prop1, "my updated prop1");
//            Assert.assertEquals(v1.getDescription(), "my feed desc");
//            Assert.assertEquals(v11.getDescription(), "My Feed Updated Description");
//            String v = v11.getVersionName();
//            Feed.ID v1Id = v1.getId();
//            Feed.ID v11Id = v11.getId();
//            Feed.ID baseId = baseVersion.getId();
            //assert all ids are equal
//            Assert.assertEquals(v1Id, v11Id);
//            Assert.assertEquals(v1Id, baseId);
            return f.getId();
        }, MetadataAccess.SERVICE);


    }

    private int printVersions(JcrObject o) {
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
        return versionCount;
    }

    @Test
    public void queryTest() {

        //final String query = "select e.* from [tba:feed] as e  join [tba:category] c on e.[tba:category].[tba:systemName] = c.[tba:systemName] where  c.[tba:systemName] = $category ";
        final String query = "select e.* from [tba:feed] as e join [tba:category] as c on e.[tba:category] = c.[jcr:uuid]";

        metadata.read(() -> {
            List<Node> feeds = ((JcrFeedProvider) feedProvider).findNodes(query);
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            List<Category> c = categoryProvider.findAll();
            if (c != null) {
                for (Category cat : c) {
                    Category jcrCategory = (Category) cat;
                    List<? extends Feed> categoryFeeds = jcrCategory.getFeeds();
                    if (categoryFeeds != null) {
                        for (Feed feed : categoryFeeds) {
                            log.info("Feed for category {} is {}", cat.getSystemName(), feed.getName());
                        }
                    }

                }
            }
        }, MetadataAccess.SERVICE);

    }

    @Test
    public void testFeedManager() {
        Feed feed = metadata.read(() -> {
            List<Feed> feeds = feedProvider.findAll();
            if (feeds != null) {
                return feeds.get(0);
            }
            return null;
        }, MetadataAccess.SERVICE);

    }

    @Test
    public void testMergeProps() {
        testFeed();
        Map<String, Object> props = new HashMap<>();
        props.put("name", "An Old User");
        props.put("age", 140);

        Map<String, Object> props2 = metadata.commit(() -> {
            List<? extends Feed> feeds = feedProvider.getFeeds();
            Feed feed = null;
            //grab the first feed
            if (feeds != null) {
                feed = feeds.get(0);
            }
            feedProvider.mergeFeedProperties(feed.getId(), props);
            return feed.getProperties();
        }, MetadataAccess.SERVICE);
        props.put("address", "Some road");
        props2 = metadata.commit(() -> {
            List<? extends Feed> feeds = feedProvider.getFeeds();
            Feed feed = null;
            //grab the first feed
            if (feeds != null) {
                feed = feeds.get(0);
            }

            feedProvider.mergeFeedProperties(feed.getId(), props);
            return feed.getProperties();
        }, MetadataAccess.SERVICE);
        org.junit.Assert.assertEquals("Some road", props2.get("address"));

        //Now Replace
        props.remove("address");
        props2 = metadata.commit(() -> {
            List<? extends Feed> feeds = feedProvider.getFeeds();
            Feed feed = null;
            //grab the first feed
            if (feeds != null) {
                feed = feeds.get(0);
            }
            feedProvider.replaceProperties(feed.getId(), props);
            return feed.getProperties();
        }, MetadataAccess.SERVICE);
        Assert.assertNull(props2.get("address"));


    }
}






