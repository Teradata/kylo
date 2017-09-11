/**
 *
 */
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

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrFeedSecurityTestConfig.class })
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class JcrFeedRolesTest {
    
    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");
    private static final UsernamePrincipal TEST_USER3 = new UsernamePrincipal("tester3");

    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private CategoryProvider categoryProvider;
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    
    private JcrTool tool = new JcrTool(true, System.out);

    private String categoryName;
    private Feed.ID idA;
    private Feed.ID idB;
    private Feed.ID idC;
    
    @Before
    public void createFeeds() {
        this.categoryName = metadata.commit(() -> {
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER2));
            
            this.roleProvider.createRole(SecurityRole.FEED, "testEditor", "Editor", "Can edit feeds")
                .setPermissions(FeedAccessControl.EDIT_DETAILS, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
            this.roleProvider.createRole(SecurityRole.FEED, "testViewer", "Viewer", "Can view feeds only")
                .setPermissions(FeedAccessControl.ACCESS_FEED);
            
            Category cat = categoryProvider.ensureCategory("test");
            cat.getAllowedActions().enableAll(TEST_USER1);
            cat.getAllowedActions().enableAll(TEST_USER2);
            
            return cat.getSystemName();
        }, JcrMetadataAccess.SERVICE);
        
        this.idA = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedA");
            feed.setDescription("Feed A");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, JcrMetadataAccess.SERVICE);
        
        this.idB = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedB");
            feed.setDescription("Feed B");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, JcrMetadataAccess.SERVICE);
        
        this.idC = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedC");
            feed.setDescription("Feed C");
            feed.setJson("{ \"property\":\"value\" }");
            feed.setState(State.ENABLED);
            return feed.getId();
        }, JcrMetadataAccess.SERVICE);
        
//        metadata.commit(() -> tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata/feeds/test"), JcrMetadataAccess.SERVICE);
    }
    
    @Test
    public void testSeeOnlyOwnFeeds() {
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER1));
            this.feedProvider.findById(idB).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER2));
            this.feedProvider.findById(idB).getRoleMembership("testViewer").ifPresent(m -> m.addMember(TEST_USER1));
            this.feedProvider.findById(idC).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER2));
        }, JcrMetadataAccess.SERVICE);

        int feedCnt1 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER1);
        
        assertThat(feedCnt1).isEqualTo(2);
        
        int feedCnt2 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER2);
        
        assertThat(feedCnt2).isEqualTo(2);
        
        int feedCnt3 = metadata.read(() -> this.feedProvider.getFeeds().size(), TEST_USER3);
        
        assertThat(feedCnt3).isEqualTo(0);
    }
    
    @Test
    public void testAddMembership() {
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            
            assertThat(feedA).isNull();
            
            Feed feedB = this.feedProvider.getFeed(idB);
            
            assertThat(feedB).isNull();
        }, TEST_USER3);
        
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getRoleMembership("testViewer").ifPresent(m -> m.addMember(TEST_USER3));
            this.feedProvider.findById(idB).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER3));
        }, JcrMetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            
            assertThat(feedA.getDescription()).isNotNull().isEqualTo("Feed A");
            assertThat(feedA.getJson()).isNull();
            assertThat(feedA.getState()).isNull();
            
            Feed feedB = this.feedProvider.getFeed(idB);
            
            assertThat(feedB.getDescription()).isNotNull().isEqualTo("Feed B");
            assertThat(feedB.getJson()).isNotNull();
            assertThat(feedB.getState()).isNotNull();
        }, TEST_USER3);
    }
    
    @Test
    public void testRemoveMembership() {
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getRoleMembership("testViewer").ifPresent(m -> m.addMember(TEST_USER3));
            this.feedProvider.findById(idA).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER3));
            this.feedProvider.findById(idB).getRoleMembership("testEditor").ifPresent(m -> m.addMember(TEST_USER3));
        }, JcrMetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            
            assertThat(feedA.getDescription()).isNotNull().isEqualTo("Feed A");
            assertThat(feedA.getJson()).isNotNull();
            assertThat(feedA.getState()).isNotNull();
            
            Feed feedB = this.feedProvider.getFeed(idB);
            
            assertThat(feedB.getDescription()).isNotNull().isEqualTo("Feed B");
            assertThat(feedB.getJson()).isNotNull();
            assertThat(feedB.getState()).isNotNull();
        }, TEST_USER3);
        
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getRoleMembership("testEditor").ifPresent(m -> m.removeMember(TEST_USER3));
        }, JcrMetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            
            assertThat(feedA.getDescription()).isNotNull().isEqualTo("Feed A");
            assertThat(feedA.getJson()).isNull();
            assertThat(feedA.getState()).isNull();
            
            Feed feedB = this.feedProvider.getFeed(idB);
            
            assertThat(feedB.getDescription()).isNotNull().isEqualTo("Feed B");
            assertThat(feedB.getJson()).isNotNull();
            assertThat(feedB.getState()).isNotNull();
        }, TEST_USER3);
        
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getRoleMembership("testViewer").ifPresent(m -> m.removeMember(TEST_USER3));
            this.feedProvider.findById(idB).getRoleMembership("testEditor").ifPresent(m -> m.removeMember(TEST_USER3));
        }, JcrMetadataAccess.SERVICE);
        
        metadata.read(() -> {
            Feed feedA = this.feedProvider.getFeed(idA);
            
            assertThat(feedA).isNull();
            
            Feed feedB = this.feedProvider.getFeed(idB);
            
            assertThat(feedB).isNull();
        }, TEST_USER3);
    }
}
