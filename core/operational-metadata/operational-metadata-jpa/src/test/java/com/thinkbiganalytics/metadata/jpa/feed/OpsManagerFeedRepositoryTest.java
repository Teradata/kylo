package com.thinkbiganalytics.metadata.jpa.feed;

/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlConfig;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;
import com.thinkbiganalytics.test.security.WithMockJaasUser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;


@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(
    classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class, OpsManagerFeedRepositoryTest.class, FeedOpsAccessControlConfig.class})
@Transactional
@Configuration
public class OpsManagerFeedRepositoryTest {

    @Bean
    public AccessController accessController() {
        AccessController mock = Mockito.mock(AccessController.class);
        Mockito.when(mock.isEntityAccessControlled()).thenReturn(true);
        return mock;
    }

    @Autowired
    TestOpsManagerFeedRepository repo;

    @Autowired
    FeedOpsAccessControlRepository aclRepo;


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedUsingPrincipalsName_MatchingUserNameAndFeedName() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "dladmin");
        repo.save(feed);
        List<String> feedNames = repo.getFeedNamesForCurrentUser();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("dladmin", feedNames.get(0));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedUsingPrincipalsName_NonMatchingUserNameAndFeedName() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "non-matching-feed-name");
        repo.save(feed);
        List<String> feedNames = repo.getFeedNamesForCurrentUser();
        Assert.assertEquals(0, feedNames.size());
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedNames_MatchingGroupAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);
        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("feed-name", feedNames.get(0));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedNames_NoMatchingGroupMatchingUserAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);
        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(1, feedNames.size());
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedNames_NoMatchingGroupAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);
        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(0, feedNames.size());
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedNames_BothMatchingAndNonMatchingGroupsAreSetInAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);
        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("feed-name", feedNames.get(0));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findFeedNames_MultipleMatchingFeedsAndGroups() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed1.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);

        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);

        BaseFeed.FeedId feedId2 = new BaseFeed.FeedId(feed2.getId().getUuid());

        JpaFeedOpsAclEntry userGroupAcl = new JpaFeedOpsAclEntry(feedId2, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(userGroupAcl);

        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(2, feedNames.size());
        Assert.assertTrue(feedNames.contains("feed1-name"));
        Assert.assertTrue(feedNames.contains("feed2-name"));
    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_NoMatchingGroupMatchingUserAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);
        Iterable<JpaOpsManagerFeed> all = repo.findAll();
        Assert.assertTrue(StreamSupport.stream(all.spliterator(), false)
                              .allMatch(it -> it.getName().equals("feed-name")));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_NoMatchingGroupAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);
        Iterable<JpaOpsManagerFeed> all = repo.findAll();
        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getName().equals("feed-name")));
    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAllFilter_NoMatchingGroupMatchingUserAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        QJpaOpsManagerFeed qFeed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        Iterable<JpaOpsManagerFeed> all = repo.findAll(GenericQueryDslFilter.buildFilter(qFeed, "name: feed-name"));
        Assert.assertTrue(StreamSupport.stream(all.spliterator(), false)
                              .allMatch(it -> it.getName().equals("feed-name")));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAllFilter_NoMatchingGroupAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        QJpaOpsManagerFeed qFeed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        Iterable<JpaOpsManagerFeed> all = repo.findAll(GenericQueryDslFilter.buildFilter(qFeed, "name: feed-name"));
        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getName().equals("feed-name")));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAllFilter_MatchingGroupButNoMatchingFilter() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        QJpaOpsManagerFeed qFeed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        Iterable<JpaOpsManagerFeed> all = repo.findAll(GenericQueryDslFilter.buildFilter(qFeed, "name==non-matching-feed-name"));

        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getName().equals("feed-name")));

        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getName().equals("non-matching-feed-name")));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAllFilter_MatchingGroupAndMatchingFilter() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry matchingGroup = new JpaFeedOpsAclEntry(feedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(matchingGroup);

        JpaOpsManagerFeed nonMatchingFeed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "non-matching-feed-name");
        repo.save(nonMatchingFeed);

        BaseFeed.FeedId nonMatchingFeedId = new BaseFeed.FeedId(nonMatchingFeed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl1 = new JpaFeedOpsAclEntry(nonMatchingFeedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl1);

        JpaFeedOpsAclEntry matchingGroup1 = new JpaFeedOpsAclEntry(nonMatchingFeedId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(matchingGroup1);

        QJpaOpsManagerFeed qFeed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        Iterable<JpaOpsManagerFeed> all = repo.findAll(GenericQueryDslFilter.buildFilter(qFeed, "name==feed-name"));

        Assert.assertTrue(StreamSupport.stream(all.spliterator(), false)
                              .anyMatch(it -> it.getName().equals("feed-name")));

        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getName().equals("non-matching-feed-name")));
    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void count_ShouldCountOnlyPermittedFeeds() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);

        BaseFeed.FeedId feed1Id = new BaseFeed.FeedId(feed1.getId().getUuid());

        JpaFeedOpsAclEntry acl1 = new JpaFeedOpsAclEntry(feed1Id, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl1);

        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);

        BaseFeed.FeedId feed2Id = new BaseFeed.FeedId(feed2.getId().getUuid());

        JpaFeedOpsAclEntry acl2 = new JpaFeedOpsAclEntry(feed2Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl2);

        JpaOpsManagerFeed feed3 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed3-name");
        repo.save(feed3);

        BaseFeed.FeedId feed3Id = new BaseFeed.FeedId(feed3.getId().getUuid());

        JpaFeedOpsAclEntry acl3 = new JpaFeedOpsAclEntry(feed3Id, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl3);

        long count = repo.count();
        Assert.assertEquals(2, count);

        List<JpaOpsManagerFeed> feeds = repo.findAll();
        Assert.assertTrue(feeds.stream()
                              .anyMatch(it -> it.getName().equals("feed1-name")));
        Assert.assertTrue(feeds.stream()
                              .anyMatch(it -> it.getName().equals("feed2-name")));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findOne() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);

        BaseFeed.FeedId feed1Id = new BaseFeed.FeedId(feed1.getId().getUuid());

        JpaFeedOpsAclEntry acl1 = new JpaFeedOpsAclEntry(feed1Id, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl1);

        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);

        BaseFeed.FeedId feed2Id = new BaseFeed.FeedId(feed2.getId().getUuid());

        JpaFeedOpsAclEntry acl2 = new JpaFeedOpsAclEntry(feed2Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl2);

        JpaOpsManagerFeed feed3 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed3-name");
        repo.save(feed3);

        BaseFeed.FeedId feed3Id = new BaseFeed.FeedId(feed3.getId().getUuid());

        JpaFeedOpsAclEntry acl3 = new JpaFeedOpsAclEntry(feed3Id, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl3);
        Assert.assertNotNull(repo.findOne(feed1.getId()));
        Assert.assertNotNull(repo.findOne(feed2.getId()));
        Assert.assertNull(repo.findOne(feed3.getId()));
    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_TwoPages() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);
        BaseFeed.FeedId feed1Id = new BaseFeed.FeedId(feed1.getId().getUuid());
        JpaFeedOpsAclEntry acl1 = new JpaFeedOpsAclEntry(feed1Id, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl1);

        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);
        BaseFeed.FeedId feed2Id = new BaseFeed.FeedId(feed2.getId().getUuid());
        JpaFeedOpsAclEntry acl2 = new JpaFeedOpsAclEntry(feed2Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl2);

        JpaOpsManagerFeed feed3 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed3-name");
        repo.save(feed3);
        BaseFeed.FeedId feed3Id = new BaseFeed.FeedId(feed3.getId().getUuid());
        JpaFeedOpsAclEntry acl3 = new JpaFeedOpsAclEntry(feed3Id, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl3);

        JpaOpsManagerFeed feed4 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed4-name");
        repo.save(feed4);
        BaseFeed.FeedId feed4Id = new BaseFeed.FeedId(feed4.getId().getUuid());
        JpaFeedOpsAclEntry acl4 = new JpaFeedOpsAclEntry(feed4Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl4);

        JpaOpsManagerFeed feed5 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed5-name");
        repo.save(feed5);
        BaseFeed.FeedId feed5Id = new BaseFeed.FeedId(feed5.getId().getUuid());
        JpaFeedOpsAclEntry acl5 = new JpaFeedOpsAclEntry(feed5Id, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl5);

        JpaOpsManagerFeed feed6 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed6-name");
        repo.save(feed6);
        BaseFeed.FeedId feed6Id = new BaseFeed.FeedId(feed6.getId().getUuid());
        JpaFeedOpsAclEntry acl6 = new JpaFeedOpsAclEntry(feed6Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl6);

        Pageable page1Request = new PageRequest(0, 2);
        Page<JpaOpsManagerFeed> page1 = repo.findAll(page1Request);
        Assert.assertEquals(0, page1.getNumber());
        Assert.assertEquals(2, page1.getNumberOfElements());
        Assert.assertEquals(2, page1.getTotalPages());
        Assert.assertEquals(4, page1.getTotalElements());

        Pageable page2Request = new PageRequest(1, 2);
        Page<JpaOpsManagerFeed> page2 = repo.findAll(page2Request);
        Assert.assertEquals(1, page2.getNumber());
        Assert.assertEquals(2, page2.getNumberOfElements());
        Assert.assertEquals(2, page2.getTotalPages());
        Assert.assertEquals(4, page2.getTotalElements());

    }


    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void testCustomMethod_findByName() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);
        BaseFeed.FeedId feed1Id = new BaseFeed.FeedId(feed1.getId().getUuid());
        JpaFeedOpsAclEntry acl1 = new JpaFeedOpsAclEntry(feed1Id, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl1);

        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);
        BaseFeed.FeedId feed2Id = new BaseFeed.FeedId(feed2.getId().getUuid());
        JpaFeedOpsAclEntry acl2 = new JpaFeedOpsAclEntry(feed2Id, "user", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(acl2);

        List<JpaOpsManagerFeed> feeds1 = repo.findByName("feed1-name");
        Assert.assertTrue(feeds1.isEmpty());

        List<JpaOpsManagerFeed> feeds2 = repo.findByName("feed2-name");
        Assert.assertEquals(1, feeds2.size());
        Assert.assertEquals("feed2-name", feeds2.get(0).getName());
    }


}
