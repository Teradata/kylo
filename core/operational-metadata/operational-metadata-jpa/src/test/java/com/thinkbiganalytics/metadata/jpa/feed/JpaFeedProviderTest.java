/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import javax.inject.Inject;


@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
public class JpaFeedProviderTest {

    @Inject
    private OpsFeedManagerFeedProvider feedProvider;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;

    private String nameTag = "" + System.currentTimeMillis();

    @Test
    public void testCreateFeed() {
        String name = "test.category.testFeed";
        operationalMetadataAccess.commit(() -> {
            OpsManagerFeed.ID id = feedProvider.resolveId(UUID.randomUUID().toString());
            return feedProvider.save(id, name);
        });

        operationalMetadataAccess.read(() -> {
            OpsManagerFeed feed = feedProvider.findByName(name);
            Assert.assertNotNull(feed);
            return feed;
        });


    }
}
