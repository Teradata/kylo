package com.thinkbiganalytics.metadata.jpa.feed;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;
import com.thinkbiganalytics.test.security.WithMockJaasUser;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class, JpaFeedProviderTest.class})
public class JpaFeedProviderTest {

    @Inject
    private FeedOpsAccessControlProvider aclProvider;

    @Inject
    private OpsFeedManagerFeedProvider feedProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Bean
    public AccessController accessController() {
        AccessController mock = Mockito.mock(AccessController.class);
        Mockito.when(mock.isEntityAccessControlled()).thenReturn(true);
        return mock;
    }
    

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testFindFeedUsingGenericFilter() {
        // Create feed
        final String name = "testCategory.testFeed";
        final String id = metadataAccess.commit(() -> {
            final OpsManagerFeed.ID feedId = feedProvider.resolveId(UUID.randomUUID().toString());
            feedProvider.save(feedId, name,false,1000L);
            return feedId.toString();
        });

        // Add ACL entries
        final BaseFeed.FeedId feedId = new BaseFeed.FeedId(id);
        final JpaFeedOpsAclEntry userAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        final JpaFeedOpsAclEntry adminAcl = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclProvider.grantAccess(feedId,new UsernamePrincipal("dladmin"), new GroupPrincipal("admin"));

        // Verify access to feeds
        metadataAccess.read(() -> {
            List<OpsManagerFeed> feeds = feedProvider.findAll("name:" + name);
            Assert.assertTrue(feeds != null && !feeds.isEmpty());

            List<String> feedNames = feedProvider.getFeedNames();
            Assert.assertTrue(feedNames != null && !feedNames.isEmpty());

            return feeds;
        });
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testFeedHealth() {
        metadataAccess.read(() -> {
            List<? extends com.thinkbiganalytics.metadata.api.feed.FeedHealth> health = feedProvider.getFeedHealth();

            return null;
        });
    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testJobStatusCountFromNow() {
        String feedName = "movies.new_releases";
        metadataAccess.read(() -> {
            Period period = DateTimeUtil.period("10W");
            List<JobStatusCount> counts = feedProvider.getJobStatusCountByDateFromNow(feedName, period);
            return counts;
        });

    }

//    @Test
//    public void testAbandonFeedJobs() {
//
//        try (AbandonFeedJobsStoredProcedureMock storedProcedureMock = new AbandonFeedJobsStoredProcedureMock()) {
//
//            Assert.assertTrue(storedProcedureMock.getInvocationParameters().isEmpty());
//
//            String feedName = "movies.new_releases";
//            metadataAccess.commit(() -> {
//                feedProvider.abandonFeedJobs(feedName);
//            });
//
//            Assert.assertFalse(storedProcedureMock.getInvocationParameters().isEmpty());
//
//            Assert.assertEquals(1, storedProcedureMock.getInvocationParameters().size());
//
//            AbandonFeedJobsStoredProcedureMock.InvocationParameters parameters =
//                    storedProcedureMock.getInvocationParameters().get(0);
//
//            Assert.assertEquals(feedName, parameters.feed);
//
//            String expectedExitMessagePrefix = String.format("Job manually abandoned @ %s",
//                    DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now()));
//
//            Assert.assertTrue(parameters.exitMessage.startsWith(expectedExitMessagePrefix));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
