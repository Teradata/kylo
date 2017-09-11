package com.thinkbiganalytics.metadata.modeshape.feed.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrFeedSecurityTestConfig.class})
public class JcrFeedEnableAccessControlTest {

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
    
    @Inject
    private AccessController accessController;

    private String categoryName;
    private Feed.ID idA;
    private Feed.ID idB;
    private Feed.ID idC;

    @Before
    public void createCategoryFeeds() {
        when(this.accessController.isEntityAccessControlled()).thenReturn(true);
        
        categoryName = metadata.commit(() -> {
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER2));
            Category cat = categoryProvider.ensureCategory("test");
            cat.getAllowedActions().enableAll(TEST_USER1);
            cat.getAllowedActions().enableAll(TEST_USER2);
            return cat.getSystemName();
        }, JcrMetadataAccess.SERVICE);
    }

    @After
    public void cleanup() {
        metadata.commit(() -> {
            if (idC != null) this.feedProvider.deleteFeed(idC);
            if (idB != null) this.feedProvider.deleteFeed(idB);
            if (idA != null) this.feedProvider.deleteFeed(idA);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testCreateFeedsNoAccessControl() {
        when(this.accessController.isEntityAccessControlled()).thenReturn(false);
        createFeeds();
        
        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);
        
        assertThat(feedCnt1).isEqualTo(3);
        
        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);
        
        assertThat(feedCnt2).isEqualTo(3);
    }

    @Test
    public void testCreateFeedsWithAccessControl() {
        when(this.accessController.isEntityAccessControlled()).thenReturn(true);
        createFeeds();
        
        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);

        assertThat(feedCnt1).isEqualTo(1);

        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);

        assertThat(feedCnt2).isEqualTo(2);
    }
    
    @Test
    public void testEnableFeedAccessControl() {
        when(this.accessController.isEntityAccessControlled()).thenReturn(false);
        
        createFeeds();
        
        metadata.commit(() -> {
            JcrFeed feedA = (JcrFeed) this.feedProvider.getFeed(idA);
            this.actionsProvider.getAvailableActions(AllowedActions.FEED)
                .ifPresent(actions -> feedA.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), Collections.emptyList()));
        }, TEST_USER1);
        
        
        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);
        
        assertThat(feedCnt1).isEqualTo(3);
        
        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);
        
        assertThat(feedCnt2).isEqualTo(2);
    }
    
    @Test
    public void testDisableFeedAccessControl() {
        when(this.accessController.isEntityAccessControlled()).thenReturn(true);
        
        createFeeds();
        
        metadata.commit(() -> {
            JcrFeed feedB = (JcrFeed) this.feedProvider.getFeed(idB);
            this.actionsProvider.getAvailableActions(AllowedActions.FEED)
                .ifPresent(actions -> feedB.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
            JcrFeed feedC = (JcrFeed) this.feedProvider.getFeed(idC);
            this.actionsProvider.getAvailableActions(AllowedActions.FEED)
                .ifPresent(actions -> feedC.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
        }, TEST_USER2);
        
        
        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);
        
        assertThat(feedCnt1).isEqualTo(3);
        
        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);
        
        assertThat(feedCnt2).isEqualTo(2);
    }

    private void createFeeds() {
        this.idA = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedA");
            feed.setDescription("Feed A");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER1);
        
        this.idB = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedB");
            feed.setDescription("Feed B");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER2);
        
        this.idC = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedC");
            feed.setDescription("Feed C");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, TEST_USER2);
    }
}
