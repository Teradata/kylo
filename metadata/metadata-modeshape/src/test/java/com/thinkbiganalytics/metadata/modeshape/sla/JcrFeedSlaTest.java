package com.thinkbiganalytics.metadata.modeshape.sla;

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
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics.metadata.modeshape.op"})
public class JcrFeedSlaTest {

    private static String FEED_SLA = "feedSla";
    @Inject
    CategoryProvider categoryProvider;
    @Inject
    FeedProvider feedProvider;
    @Inject
    ServiceLevelAgreementProvider slaProvider;
    @Inject
    FeedServiceLevelAgreementProvider feedSlaProvider;
    @Inject
    private ExtensibleTypeProvider typeProvider;
    @Inject
    private ExtensibleEntityProvider entityProvider;
    @Inject
    private JcrMetadataAccess metadata;

    public Set<Feed.ID> createFeeds(int number) {
        Set<Feed.ID> feedIds = new HashSet<>();
        String categorySystemName = "my_category";
        for (int i = 0; i < number; i++) {
            JcrFeed.FeedId feedId = createFeed(categorySystemName, "my_feed" + i);
            feedIds.add(feedId);
        }
        return feedIds;

    }


    public JcrFeed.FeedId createFeed(final String categorySystemName, final String feedSystemName) {

        Category category = metadata.commit(() -> {
            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
            cat.setDescription(categorySystemName + " desc");
            cat.setTitle(categorySystemName);
            categoryProvider.update(cat);
            return cat;
        }, MetadataAccess.ADMIN);

        return metadata.commit(() -> {
            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
            JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(categorySystemName, feedSystemName, feedSystemName + " desc");

            feed.setTitle(feedSystemName);
            return feed.getId();
        }, MetadataAccess.ADMIN);
    }


    @Before
    public void setUp() throws Exception {
        JcrFeedServiceLevelAgreementProvider jcrFeedSlaProvider = (JcrFeedServiceLevelAgreementProvider) feedSlaProvider;
        jcrFeedSlaProvider.createType();
    }

    @Test
    public void testCreateFeedSLAEntity() {
        //create 2 feeds
        final int numberOfFeeds = 2;
        Set<Feed.ID> feedIds = createFeeds(numberOfFeeds);
        final String feedSlaTitle = "My New SLA";
        final String nonFeedSlaTitle = "No Feed SLA";
        ExtensibleEntity.ID feedSlaEntityId = createFeedSLAEntity(feedIds, feedSlaTitle);
        ServiceLevelAgreement.ID nonFeedSla = createGenericSla(nonFeedSlaTitle);

        ServiceLevelAgreement.ID slaId = metadata.read(() -> {

            JcrFeedServiceLevelAgreementProvider jcrFeedSlaProvider = (JcrFeedServiceLevelAgreementProvider) feedSlaProvider;

            //ASSERT everything is good

            //Assert query returns the correct result
            List<ExtensibleEntity> entities = jcrFeedSlaProvider.findAllRelationships();
            Assert.assertEquals(entities.size(), 1);

            //Assert relationships are correct
            JcrFeedServiceLevelAgreementRelationship entity = (JcrFeedServiceLevelAgreementRelationship) jcrFeedSlaProvider.getRelationship(feedSlaEntityId);
            ServiceLevelAgreement feedSla = entity.getAgreement();
            Assert.assertNotNull(feedSla);

            List<? extends ServiceLevelAgreement> agreements = slaProvider.getAgreements();
            //assert both agreements are there
            Assert.assertEquals(agreements.size(), 2);

            Set<JcrFeed> feeds = entity.getPropertyAsSet(JcrFeedServiceLevelAgreementRelationship.FEEDS, JcrFeed.class);
            Assert.assertEquals(feeds.size(), numberOfFeeds);
            for (JcrFeed feed : feeds) {
                Assert.assertTrue(feedIds.contains(feed.getId()));
            }

            //find it by the SLA now
            JcrFeedServiceLevelAgreementRelationship finalFeedSla = (JcrFeedServiceLevelAgreementRelationship) jcrFeedSlaProvider.findRelationship(feedSla.getId());
            Assert.assertNotNull(finalFeedSla);

            //query for SLA objects and assert the result is correct
            List<FeedServiceLevelAgreement> feedAgreements = jcrFeedSlaProvider.findAllAgreements();

            Assert.assertEquals(feedAgreements.size(), 1);
            int nonFeedSlaCount = 0;
            for (FeedServiceLevelAgreement agreement : feedAgreements) {
                Set<? extends Feed> slaFeeds = agreement.getFeeds();
                String title = agreement.getName();
                if (slaFeeds != null) {
                    Assert.assertEquals(title, feedSlaTitle);
                    Assert.assertEquals(slaFeeds.size(), numberOfFeeds);
                    for (Feed feed : slaFeeds) {
                        Assert.assertTrue(feedIds.contains(feed.getId()));
                    }
                } else {
                    Assert.assertEquals(title, nonFeedSlaTitle);
                    nonFeedSlaCount++;
                }

            }
            Assert.assertEquals(nonFeedSlaCount, 0);

            //find by Feed
            for (Feed.ID feedId : feedIds) {
                List<FeedServiceLevelAgreement> feedServiceLevelAgreements = jcrFeedSlaProvider.findFeedServiceLevelAgreements(feedId);
                Assert.assertTrue(feedServiceLevelAgreements != null && !feedServiceLevelAgreements.isEmpty());

            }

            return feedSla.getId();
        }, MetadataAccess.SERVICE);

        ExtensibleEntity entity = metadata.read(() -> {
            ExtensibleEntity e = entityProvider.getEntity(feedSlaEntityId);
            Set<Node> feeds = (Set<Node>) e.getPropertyAsSet(JcrFeedServiceLevelAgreementRelationship.FEEDS, Node.class);
            Assert.assertEquals(feeds.size(), numberOfFeeds);
            for (Node feed : feeds) {
                try {
                    Assert.assertTrue(feedIds.contains(feedProvider.resolveFeed(feed.getIdentifier())));
                } catch (RepositoryException e1) {
                    e1.printStackTrace();
                }
            }
            return e;
        }, MetadataAccess.SERVICE);

        //now remove the feed relationships
        boolean removedFeedRelationships = metadata.commit(() -> {
            ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
            return feedSlaProvider.removeFeedRelationships(slaId);

        }, MetadataAccess.SERVICE);

        //query for the feeds related to this SLA and verify there are none
        metadata.read(() -> {
            FeedServiceLevelAgreement feedServiceLevelAgreement = feedSlaProvider.findAgreement(slaId);
            Assert.assertTrue(feedServiceLevelAgreement.getFeeds() == null || (feedServiceLevelAgreement.getFeeds().isEmpty()));
            return null;
        }, MetadataAccess.SERVICE);


    }


    public ExtensibleEntity.ID createFeedSLAEntity(Set<Feed.ID> feedIdList, String title) {
        return metadata.commit(() -> {
            ServiceLevelAgreement sla = slaProvider.builder().name(title).description(title + " DESC").build();
            ExtensibleEntity entity = feedSlaProvider.relate(sla, feedIdList);
            return entity.getId();
        }, MetadataAccess.SERVICE);

    }

    public ServiceLevelAgreement.ID createGenericSla(String title) {
        return metadata.commit(() -> {
            ServiceLevelAgreement sla = slaProvider.builder().name(title).description(title + " DESC").build();
            return sla.getId();
        }, MetadataAccess.SERVICE);

    }


}
