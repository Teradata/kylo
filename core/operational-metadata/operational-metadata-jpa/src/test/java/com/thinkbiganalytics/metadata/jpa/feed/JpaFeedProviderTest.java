/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;


@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
public class JpaFeedProviderTest {

    @Inject
    private OpsFeedManagerFeedProvider feedProvider;

    @Inject
    private MetadataAccess metadataAccess;


    @Test
    public void testFindFeedUsingGenericFilter() {
        String name = "testCategory.testFeed";
        metadataAccess.commit(() -> {
            OpsManagerFeed.ID id = feedProvider.resolveId(UUID.randomUUID().toString());
            return feedProvider.save(id, name);
        });

        metadataAccess.read(() -> {
            List<OpsManagerFeed> feeds = feedProvider.findAll("name:"+name);
            Assert.assertTrue(feeds != null && !feeds.isEmpty());

            List<String> feedNames = feedProvider.getFeedNames();
            Assert.assertTrue(feedNames != null && !feedNames.isEmpty());

            return feeds;
        });


    }

    @Test
    public void testFeedHealth(){
        metadataAccess.read(() -> {
          List<? extends com.thinkbiganalytics.metadata.api.feed.FeedHealth> health =  feedProvider.getFeedHealth();

            return null;
        });
    }


    @Test
    public void testJobStatusCountFromNow(){
        String feedName = "movies.new_releases";
        metadataAccess.read(() -> {
            Period period = DateTimeUtil.period("10W");
            List<JobStatusCount> counts = feedProvider.getJobStatusCountByDateFromNow(feedName,period);
            return counts;
        });

    }
}
