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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.feed.security.JcrFeedSecurityTestConfig;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAuthConfig;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, ModeShapeAuthConfig.class})
public class JcrCategoryAllowedActionsTest {

    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");

    @Inject
    private MetadataAccess metadata;

    @Inject
    private CategoryProvider categoryProvider;

    private Category.ID idA;
    private Category.ID idB;
    private Category.ID idC;

    @Before
    public void createCategories() {
        idA = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testA");
            cat.setDisplayName("Test A");
            cat.setDescription("Test A descr");
            cat.setUserProperties(Arrays.asList("a1", "a2", "a3").stream().collect(Collectors.toMap(Object::toString, s -> s + " value")), Collections.emptySet());
            return cat.getId();
        }, TEST_USER1);

        idB = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testB");
            cat.setDisplayName("Test B");
            cat.setDescription("Test B descr");
            cat.setUserProperties(Arrays.asList("b1", "b2", "b3").stream().collect(Collectors.toMap(Object::toString, s -> s + " value")), Collections.emptySet());
            return cat.getId();
        }, TEST_USER2);

        idC = metadata.commit(() -> {
            Category cat = categoryProvider.ensureCategory("testC");
            cat.setDisplayName("Test C");
            cat.setDescription("Test C descr");
            cat.setUserProperties(Arrays.asList("c1", "c2", "c3").stream().collect(Collectors.toMap(Object::toString, s -> s + " value")), Collections.emptySet());
            return cat.getId();
        }, TEST_USER2);
    }

    @After
    public void cleanup() {
        metadata.commit(() -> {
            Category a = categoryProvider.findBySystemName("testA");
            if(a != null) {
                categoryProvider.deleteById(a.getId());
            }
            Category b = categoryProvider.findBySystemName("testB");
            if(b != null) {
                categoryProvider.deleteById(b.getId());
            }
            Category c = categoryProvider.findBySystemName("testC");
            if(c != null) {
                categoryProvider.deleteById(c.getId());
            }
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testSeeOnlyOwnCategories() {
        int catCnt1 = metadata.read(() -> this.categoryProvider.findAll().size(), TEST_USER1);

        assertThat(catCnt1).isEqualTo(1);

        int catCnt2 = metadata.read(() -> this.categoryProvider.findAll().size(), TEST_USER2);

        assertThat(catCnt2).isEqualTo(2);
    }

    @Test
    public void testSeeOwnContentOnly() {
        metadata.read(() -> {
            Category catA = this.categoryProvider.findById(idA);

            assertThat(catA.getDisplayName()).isNotNull().isEqualTo("Test A");
            assertThat(catA.getDescription()).isNotNull().isEqualTo("Test A descr");
            assertThat(catA.getSecurityGroups()).isNotNull();
            assertThat(catA.getUserProperties()).isNotNull();

            Category catB = this.categoryProvider.findById(idB);

            assertThat(catB).isNull();
        }, TEST_USER1);
    }

    @Test
    public void testSummaryOnlyRead() {
        Object[] nameDescr = metadata.commit(() -> {
            Category cat = this.categoryProvider.findById(idB);
            cat.getAllowedActions().enable(TEST_USER1, CategoryAccessControl.ACCESS_CATEGORY);
            return new String[]{cat.getSystemName(), cat.getDescription()};
        }, TEST_USER2);

        metadata.read(() -> {
            Category cat = this.categoryProvider.findById(idB);

            assertThat(cat).extracting("systemName", "description").contains(nameDescr);

            assertThat(cat.getUserProperties()).isEmpty();
        }, TEST_USER1);
    }
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
}
