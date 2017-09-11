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

import static org.mockito.Mockito.verify;

import javax.inject.Inject;

import org.junit.Before;
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
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
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
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class, JcrFeedSecurityTestConfig.class })
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class JcrFeedOpsPermissionTest {
    
    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");

    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private CategoryProvider categoryProvider;
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private FeedOpsAccessControlProvider opsAccessProvider;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    private String categoryName;
    private Feed.ID idA;
    private Feed.ID idB;
    
    @Before
    public void createFeeds() {
        this.categoryName = metadata.commit(() -> {
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.SERVICES).ifPresent(allowed -> allowed.enableAll(TEST_USER2));
            
            Category cat = categoryProvider.ensureCategory("test");
            cat.getAllowedActions().enableAll(TEST_USER1);
            cat.getAllowedActions().enableAll(TEST_USER2);
            
            return cat.getSystemName();
        }, JcrMetadataAccess.SERVICE);
        
        this.idA = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedA");
            return feed.getId();
        }, JcrMetadataAccess.SERVICE);
        
        this.idB = metadata.commit(() -> {
            Feed feed = this.feedProvider.ensureFeed(categoryName, "FeedB");
            return feed.getId();
        }, JcrMetadataAccess.SERVICE);
    }
    
    @Test
    public void testGrantOpsPermission() {
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getAllowedActions().enable(TEST_USER1, FeedAccessControl.ACCESS_OPS);
        }, JcrMetadataAccess.SERVICE);
        
        verify(this.opsAccessProvider).grantAccess(idA, TEST_USER1);
    }
    
    @Test
    public void testRevokeOpsPermission() {
        metadata.commit(() -> {
            this.feedProvider.findById(idA).getAllowedActions().disable(TEST_USER1, FeedAccessControl.ACCESS_OPS);
        }, JcrMetadataAccess.SERVICE);
        
        verify(this.opsAccessProvider).revokeAccess(idA, TEST_USER1);
    }
}
