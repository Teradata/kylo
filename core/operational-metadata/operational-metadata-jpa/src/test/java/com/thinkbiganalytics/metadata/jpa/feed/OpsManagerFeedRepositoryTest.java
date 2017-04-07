package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.core.feed.BaseFeed;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
@Transactional
public class OpsManagerFeedRepositoryTest {

    @Autowired
    TestOpsManagerFeedRepository repo;

    @Autowired
    FeedOpsAccessControlRepository aclRepo;

    @Before
    public void setup() {
    }



    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedUsingPrincipalsName_MatchingUserNameAndFeedName() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "dladmin");
        repo.save(feed);

        List<String> feedNames = repo.getFeedNamesWithPrincipal();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("dladmin", feedNames.get(0));
    }

    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedUsingPrincipalsName_NonMatchingUserNameAndFeedName() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "non-matching-feed-name");
        repo.save(feed);

        List<String> feedNames = repo.getFeedNamesWithPrincipal();
        Assert.assertEquals(0, feedNames.size());
    }

    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedNames_MatchingRoleIsSetInAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "ROLE_ADMIN", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);

        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("feed-name", feedNames.get(0));
    }

    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedNames_NoMatchingRoleIsSetInAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "ROLE_NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(0, feedNames.size());
    }

    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedNames_BothMatchingAndNonMatchingRolesAreSetInAclEntry() throws Exception {
        JpaOpsManagerFeed feed = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed-name");
        repo.save(feed);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "ROLE_NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "ROLE_ADMIN", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);

        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(1, feedNames.size());
        Assert.assertEquals("feed-name", feedNames.get(0));
    }

    @WithMockUser(username = "dladmin",
                  password = "secret",
                  roles = {"ADMIN", "DLADMIN", "USER"})
    @Test
    public void findFeedNames_MultipleMatchingFeedsAndRoles() throws Exception {
        JpaOpsManagerFeed feed1 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed1-name");
        repo.save(feed1);

        BaseFeed.FeedId feedId = new BaseFeed.FeedId(feed1.getId().getUuid());

        JpaFeedOpsAclEntry dladminUserAcl = new JpaFeedOpsAclEntry(feedId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(dladminUserAcl);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(feedId, "ROLE_NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        JpaFeedOpsAclEntry adminGroupAcl = new JpaFeedOpsAclEntry(feedId, "ROLE_ADMIN", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(adminGroupAcl);


        JpaOpsManagerFeed feed2 = new JpaOpsManagerFeed(OpsManagerFeedId.create(), "feed2-name");
        repo.save(feed2);

        BaseFeed.FeedId feedId2 = new BaseFeed.FeedId(feed2.getId().getUuid());

        JpaFeedOpsAclEntry userGroupAcl = new JpaFeedOpsAclEntry(feedId2, "ROLE_USER", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(userGroupAcl);


        List<String> feedNames = repo.getFeedNames();
        Assert.assertEquals(2, feedNames.size());
        Assert.assertTrue(feedNames.contains("feed1-name"));
        Assert.assertTrue(feedNames.contains("feed2-name"));
    }

}