/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.category.security;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrCategoryAllowedActionsTestConfig.class })
public class JcrCategoryAllowedActionsTest {
    
    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");

    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private CategoryProvider categoryProvider;
    
    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    
    private Category.ID idA;
    private Category.ID idB;
    private Category.ID idC;
    
    @Before
    public void createFeeds() {
        metadata.commit(() -> {
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER2));
        }, MetadataAccess.SERVICE);
        
        idA = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testA");
            return cat.getId();
        }, TEST_USER1);
        
        idB = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testB");
            return cat.getId();
        }, TEST_USER2);
        
        idC = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testC");
            return cat.getId();
        }, TEST_USER2);
    }
    
    @After
    public void cleanup() {
        metadata.commit(() -> { 
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testSeeOnlyOwnCategories() {
        int catCnt1 = metadata.read(() -> this.categoryProvider.findAll().size(), TEST_USER1);
        
        assertThat(catCnt1).isEqualTo(1);
        
        int catCnt2 = metadata.read(() -> this.categoryProvider.findAll().size(), TEST_USER2);
        
        assertThat(catCnt2).isEqualTo(2);
    }
//    
//    @Test
//    public void testSeeOwnFeedContentOnly() {
//        metadata.read(() -> {
//            Feed feedA = this.feedProvider.getFeed(idA);
//            
//            assertThat(feedA.getDescription()).isNotNull().isEqualTo("Feed A");
//            assertThat(feedA.getJson()).isNotNull();
//            assertThat(feedA.getState()).isNotNull();
//            
//            Feed feedB = this.feedProvider.getFeed(idB);
//            
//            assertThat(feedB).isNull();
//        }, TEST_USER1);
//    }
//    
//    @Test
//    public void testLimitRelationshipResults() {
//        metadata.commit(() -> {
//            Feed feedA = this.feedProvider.getFeed(idA);
//            Feed feedB = this.feedProvider.getFeed(idB);
//            Feed feedC = this.feedProvider.getFeed(idC);
//            
//            feedC.addDependentFeed(feedA);
//            feedC.addDependentFeed(feedB);
//        }, MetadataAccess.SERVICE);
//        
//        metadata.read(() -> {
//            Feed feedC = this.feedProvider.getFeed(idC);
//            List<Feed> deps = feedC.getDependentFeeds();
//                            
//            assertThat(deps).hasSize(1).extracting("id").contains(this.idB);
//        }, TEST_USER2);
//    }
//    
//    @Test
//    public void testSummaryOnlyRead() {
//        metadata.commit(() -> {
//            Feed feed = this.feedProvider.findById(idB);
//            feed.getAllowedActions().enable(TEST_USER1, FeedAccessControl.ACCESS_FEED);
//        }, TEST_USER2);
//        
//        metadata.read(() -> {
//            Feed feed = this.feedProvider.findById(idB);
//            
//            assertThat(feed.getName()).isNotNull().isEqualTo("FeedB");
//            assertThat(feed.getCategory()).isNotNull().hasFieldOrPropertyWithValue("name", this.categoryName);
//            
//            assertThat(feed.getJson()).isNull();
//            assertThat(feed.getState()).isNull();
//        }, TEST_USER1);
//    }
}
