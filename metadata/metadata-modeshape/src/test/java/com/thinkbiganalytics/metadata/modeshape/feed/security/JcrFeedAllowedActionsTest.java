package com.thinkbiganalytics.metadata.modeshape.feed.security;

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
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrFeedSecurityTestConfig.class})
public class JcrFeedAllowedActionsTest {

    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");

    @Inject
    private MetadataAccess metadata;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    private String categorySystemName;
    private Feed.ID idA;
    private Feed.ID idB;
    private Feed.ID idC;

    @Before
    public void createFeeds() {
        categorySystemName = metadata.commit(() -> {
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER2));
            Category cat = categoryProvider.ensureCategory("test");
            cat.getAllowedActions().enableAll(TEST_USER1);
            cat.getAllowedActions().enableAll(TEST_USER2);
            return cat.getSystemName();
        }, JcrMetadataAccess.SERVICE);

        this.idA = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categorySystemName, "FeedA");
            feed.setDescription("Feed A");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER1);

        this.idB = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categorySystemName, "FeedB");
            feed.setDescription("Feed B");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER2);

        this.idC = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categorySystemName, "FeedC");
            feed.setDescription("Feed C");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER2);
    }

    @After
    public void cleanup() {
        metadata.commit(() -> {
            this.feedProvider.deleteFeed(idC);
            this.feedProvider.deleteFeed(idB);
            this.feedProvider.deleteFeed(idA);
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testSeeOnlyOwnFeeds() {
        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);

        assertThat(feedCnt1).isEqualTo(1);

        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);

        assertThat(feedCnt2).isEqualTo(2);
    }

    @Test
    public void testSeeOwnFeedContentOnly() {
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);

            assertThat(feedA.getDescription()).isNotNull().isEqualTo("Feed A");
            assertThat(feedA.getJson()).isNotNull();
            assertThat(feedA.getState()).isNotNull();

            Feed feedB = this.feedProvider.getFeed(idB);

            assertThat(feedB).isNull();
        }, TEST_USER1);
    }

    @Test
    public void testLimitRelationshipResults() {
        metadata.commit(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            Feed feedB = this.feedProvider.getFeed(idB);
            Feed feedC = this.feedProvider.getFeed(idC);

            feedC.addDependentFeed(feedA);
            feedC.addDependentFeed(feedB);
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Feed feedC = this.feedProvider.getFeed(idC);
            List<Feed> deps = feedC.getDependentFeeds();

            assertThat(deps).hasSize(1).extracting("id").contains(this.idB);
        }, TEST_USER2);
    }

    @Test
    public void testSummaryOnlyRead() {
        metadata.commit(() -> {
            Feed feed = this.feedProvider.findById(idB);
            feed.getAllowedActions().enable(TEST_USER1, FeedAccessControl.ACCESS_FEED);
        }, TEST_USER2);

        metadata.read(() -> {
            Feed feed = this.feedProvider.findById(idB);

            assertThat(feed.getName()).isNotNull().isEqualTo("FeedB");
            assertThat(feed.getCategory()).isNotNull().hasFieldOrPropertyWithValue("systemName", this.categorySystemName);

            assertThat(feed.getJson()).isNull();
            assertThat(feed.getState()).isNull();
        }, TEST_USER1);
    }
}
