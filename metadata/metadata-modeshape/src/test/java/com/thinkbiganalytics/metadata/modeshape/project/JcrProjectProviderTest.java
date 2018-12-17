/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.project;

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
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.api.project.ProjectProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedTestConfig;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@SpringBootTest(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, FeedTestConfig.class})
public class JcrProjectProviderTest extends AbstractTestNGSpringContextTests {
    private static final Logger logger = LoggerFactory.getLogger(JcrProjectProviderTest.class);

    private static final String CATEGORY_SYSTEM_NAME = "Category1";
    private static final String PROJECT_NAME_1 = "Project1";
    private static final String TEMPLATE_NAME = "Template1";
    private static final String FEED_NAME_PREFIX = "Feed";

    @Inject
    private JcrMetadataAccess metadata;

    @Resource
    private ProjectProvider projProvider;

    @Inject
    private FeedTestUtil feedTestUtil;

    @BeforeMethod
    public void before() {
        metadata.commit(() -> {
            Project pj1 = projProvider.ensureProject(PROJECT_NAME_1);
            assertThat(pj1).isNotNull();
        }, MetadataAccess.SERVICE);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void duplicateProject() {
        metadata.commit(() -> {
            Project pj1 = projProvider.createProject(PROJECT_NAME_1);
            assertThat(pj1).isNull();
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testFindProjectByName() {
        metadata.commit(() -> {
            Project pj1 = this.projProvider.ensureProject("Project2");
            assertThat(pj1).isNotNull();

            pj1.setSystemName("ProjectName2");
            pj1.setName("Project Name 2");
            pj1.setDescription("This is a fully defined project");
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName("Project2");

            assertThat(optional.isPresent()).isTrue();

            Project project = optional.get();

            assertThat(project).extracting(Project::getSystemName, Project::getName,
                                           Project::getDescription)
                .containsExactly("ProjectName2", "Project Name 2", "This is a fully defined project");
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testCannotFindProjectByName() {
        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName("bogus");

            assertThat(optional.isPresent()).isFalse();
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testAddFeedToProject() {
        String templateName = "Template1";
        String feedName = "Feed1";

        metadata.commit(() -> {
            Feed feed = feedTestUtil.findOrCreateFeed(CATEGORY_SYSTEM_NAME, feedName, templateName);
            assertThat(feed).isNotNull().extracting(Feed::getName).containsExactly(feedName);
            assertThat(feed.getTemplate()).isNotNull().extracting(FeedManagerTemplate::getName).containsExactly(TEMPLATE_NAME);
            assertThat(feed.getCategory()).isNotNull().extracting(Category::getDisplayName).containsExactly(CATEGORY_SYSTEM_NAME);

            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();

            project.addFeed(feed);

            projProvider.update(project);
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();
            List<Feed> feeds = project.getFeeds();
            Assert.assertTrue(feeds != null && feeds.size() == 1 && feeds.get(0).getName().equals(feedName));
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testAddMultipleFeedsToProject() {
        int feedCount = 5;

        Set<String> feedNames = new HashSet<>();
        addFeedsToProject(feedCount, feedNames);

        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();
            List<Feed> feeds = project.getFeeds();
            Assert.assertTrue(feeds != null && feeds.size() == feedCount);
            Set<String> savedFeeds = feeds.stream().map(feed -> feed.getName()).collect(Collectors.toSet());
            Assert.assertTrue(feedNames.size() == savedFeeds.size() && feedNames.containsAll(savedFeeds));
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void removeFeeds() {
        int feedCount = 2;
        Set<String> feedNames = new HashSet<>();

        addFeedsToProject(feedCount, feedNames);
        metadata.commit(() -> {
            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();
            List<Feed> feeds = project.getFeeds();
            project.removeFeed(feeds.get(0));
            projProvider.update(project);
        }, MetadataAccess.SERVICE);

        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();
            List<Feed> feeds = project.getFeeds();
            Assert.assertTrue(feeds != null && feeds.size() == feedCount-1);
        }, MetadataAccess.SERVICE);
    }

    private void addFeedsToProject(int feedCount, Set<String> feedNames) {
        metadata.commit(() -> {

            Optional<Project> optional = projProvider.findProjectByName(PROJECT_NAME_1);
            Project project = optional.get();

            for(int i=0; i<feedCount; i++){
                Feed feed = feedTestUtil.findOrCreateFeed(CATEGORY_SYSTEM_NAME, FEED_NAME_PREFIX +i, TEMPLATE_NAME);
                assertThat(feed).isNotNull().extracting(Feed::getName).containsExactly(FEED_NAME_PREFIX +i);
                assertThat(feed.getTemplate()).isNotNull().extracting(FeedManagerTemplate::getName).containsExactly(TEMPLATE_NAME);
                assertThat(feed.getCategory()).isNotNull().extracting(Category::getDisplayName).containsExactly(CATEGORY_SYSTEM_NAME);
                feedNames.add(feed.getName());

                project.addFeed(feed);
            }

            projProvider.update(project);
        }, MetadataAccess.SERVICE);
    }

    @AfterMethod
    public void afterClass() {
        metadata.commit(() -> {
            Collection<Project> projects = projProvider.getProjects();
            for (Project project : projects) {
                logger.info("Deleting remaining project '{}' after tests completed.", project);
                projProvider.deleteProject(project);
            }
        }, MetadataAccess.SERVICE);
    }

}
