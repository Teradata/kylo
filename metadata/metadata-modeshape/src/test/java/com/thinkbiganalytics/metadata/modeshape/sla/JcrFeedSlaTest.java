package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.api.Command;
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
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.auth.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrExtensibleProvidersTestConfig;
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

/**
 * Created by sr186054 on 8/4/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrExtensibleProvidersTestConfig.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics.metadata.modeshape.op"})
public class JcrFeedSlaTest {

    @Inject
    private ExtensibleTypeProvider typeProvider;

    @Inject
    private ExtensibleEntityProvider entityProvider;

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    ServiceLevelAgreementProvider slaProvider;

    @Inject
    FeedServiceLevelAgreementProvider feedSlaProvider;

    @Inject
    private JcrMetadataAccess metadata;

    private static String FEED_SLA = "feedSla";


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

        Category category = metadata.commit(new AdminCredentials(), new Command<Category>() {
            @Override
            public Category execute() {
                JcrCategory category = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
                category.setDescription(categorySystemName + " desc");
                category.setTitle(categorySystemName);
                categoryProvider.update(category);
                return category;
            }
        });

        return metadata.commit(new AdminCredentials(), new Command<JcrFeed.FeedId>() {
            @Override
            public JcrFeed.FeedId execute() {

                JcrCategory category = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);

                JcrFeed feed = (JcrFeed) feedProvider.ensureFeed(categorySystemName, feedSystemName, feedSystemName + " desc");
                feed.setTitle(feedSystemName);
                return feed.getId();

            }
        });
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

        ServiceLevelAgreement.ID slaId = metadata.read(new AdminCredentials(), () -> {

            JcrFeedServiceLevelAgreementProvider jcrFeedSlaProvider = (JcrFeedServiceLevelAgreementProvider) feedSlaProvider;

            //ASSERT everything is good

            //Assert query returns the correct result
            List<ExtensibleEntity> entities = jcrFeedSlaProvider.findAllRelationships();
            Assert.assertEquals(entities.size(), 1);

            //Assert relationships are correct
            JcrFeedServiceLevelAgreementRelationship entity = (JcrFeedServiceLevelAgreementRelationship) jcrFeedSlaProvider.getRelationship(feedSlaEntityId);
            ServiceLevelAgreement feedSla = entity.getAgreement();
            Assert.assertNotNull(feedSla);

            List<ServiceLevelAgreement> agreements = slaProvider.getAgreements();
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
            //assert both agreements come back

            Assert.assertEquals(2, feedAgreements.size());
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
            Assert.assertEquals(nonFeedSlaCount, 1);

            //find by Feed
            for (Feed.ID feedId : feedIds) {
                List<FeedServiceLevelAgreement> feedServiceLevelAgreements = jcrFeedSlaProvider.findFeedServiceLevelAgreements(feedId);
                Assert.assertTrue(feedServiceLevelAgreements != null && !feedServiceLevelAgreements.isEmpty());

            }

            return feedSla.getId();
        });

        ExtensibleEntity entity = metadata.read(new AdminCredentials(), () -> {
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
        });

        //now remove the feed relationships
        boolean removedFeedRelationships = metadata.commit(new AdminCredentials(), () -> {
            ServiceLevelAgreement sla = slaProvider.getAgreement(slaId);
            return feedSlaProvider.removeFeedRelationships(slaId);

        });

        //query for the feeds related to this SLA and verify there are none
        metadata.read(new AdminCredentials(), () -> {
            FeedServiceLevelAgreement feedServiceLevelAgreement = feedSlaProvider.findAgreement(slaId);
            Assert.assertTrue(feedServiceLevelAgreement.getFeeds() == null || (feedServiceLevelAgreement.getFeeds().isEmpty()));
            return null;
        });


    }


    public ExtensibleEntity.ID createFeedSLAEntity(Set<Feed.ID> feedIdList, String title) {
        return metadata.commit(new AdminCredentials(), () -> {
            ServiceLevelAgreement sla = slaProvider.builder().name(title).description(title + " DESC").build();
            ExtensibleEntity entity = feedSlaProvider.relate(sla, feedIdList);
            return entity.getId();
        });

    }

    public ServiceLevelAgreement.ID createGenericSla(String title) {
        return metadata.commit(new AdminCredentials(), () -> {
            ServiceLevelAgreement sla = slaProvider.builder().name(title).description(title + " DESC").build();
            return sla.getId();
        });

    }


}
