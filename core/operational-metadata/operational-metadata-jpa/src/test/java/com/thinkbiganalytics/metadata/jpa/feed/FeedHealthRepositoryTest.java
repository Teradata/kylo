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
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;
import com.thinkbiganalytics.test.security.WithMockJaasUser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class,
                                           OperationalMetadataConfig.class,
                                           TestJpaConfiguration.class,
                                           FeedHealthRepositoryTest.class,
                                           FeedOpsAccessControlConfig.class})
@Transactional
@Configuration
public class FeedHealthRepositoryTest {

    @Bean
    public AccessController accessController() {
        AccessController mock = Mockito.mock(AccessController.class);
        Mockito.when(mock.isEntityAccessControlled()).thenReturn(true);
        return mock;
    }

    @Inject
    FeedHealthRepository repo;

    @Inject
    FeedOpsAccessControlRepository aclRepo;

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_NoMatchingGroupAclEntry() throws Exception {
        UUID uuid = UUID.randomUUID();

        JpaOpsManagerFeedHealth health = new JpaOpsManagerFeedHealth();
        health.setFeedId(new JpaOpsManagerFeedHealth.OpsManagerFeedHealthFeedId(uuid));

        repo.save(health);

        BaseFeed.FeedId healthId = new BaseFeed.FeedId(uuid);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(healthId, "NON_MATCHING", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        Iterable<JpaOpsManagerFeedHealth> all = repo.findAll();
        Assert.assertFalse(StreamSupport.stream(all.spliterator(), false)
                               .anyMatch(it -> it.getFeedId().getUuid().equals(uuid)));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_WithMatchingGroupAclEntry() throws Exception {
        UUID uuid = UUID.randomUUID();

        JpaOpsManagerFeedHealth health = new JpaOpsManagerFeedHealth();
        health.setFeedId(new JpaOpsManagerFeedHealth.OpsManagerFeedHealthFeedId(uuid));

        repo.save(health);

        BaseFeed.FeedId healthId = new BaseFeed.FeedId(uuid);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(healthId, "admin", JpaFeedOpsAclEntry.PrincipalType.GROUP);
        aclRepo.save(nonMatching);

        Iterable<JpaOpsManagerFeedHealth> all = repo.findAll();
        Assert.assertTrue(StreamSupport.stream(all.spliterator(), false)
                              .anyMatch(it -> it.getFeedId().getUuid().equals(uuid)));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin", "user"})
    @Test
    public void findAll_WithMatchingUserAclEntry() throws Exception {
        UUID uuid = UUID.randomUUID();

        JpaOpsManagerFeedHealth health = new JpaOpsManagerFeedHealth();
        health.setFeedId(new JpaOpsManagerFeedHealth.OpsManagerFeedHealthFeedId(uuid));

        repo.save(health);

        BaseFeed.FeedId healthId = new BaseFeed.FeedId(uuid);

        JpaFeedOpsAclEntry nonMatching = new JpaFeedOpsAclEntry(healthId, "dladmin", JpaFeedOpsAclEntry.PrincipalType.USER);
        aclRepo.save(nonMatching);

        Iterable<JpaOpsManagerFeedHealth> all = repo.findAll();
        Assert.assertTrue(StreamSupport.stream(all.spliterator(), false)
                              .anyMatch(it -> it.getFeedId().getUuid().equals(uuid)));
    }

}