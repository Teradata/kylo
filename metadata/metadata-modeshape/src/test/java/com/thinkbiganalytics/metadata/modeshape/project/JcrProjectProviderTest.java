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
import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.project.providers.ProjectProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Resource;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@SpringApplicationConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class})
public class JcrProjectProviderTest extends AbstractTestNGSpringContextTests {
    private static final Logger logger = LoggerFactory.getLogger(JcrProjectProviderAccessControlTest.class);

    @Inject
    private JcrMetadataAccess metadata;

    @Resource
    private ProjectProvider projProvider;

    @Test
    public void testCreateProject() throws Exception {
        metadata.commit(() -> {
            Project pj1 = this.projProvider.ensureProject("Project1");
            assertThat(pj1).isNotNull();

            /*Session session =  metadata.getActiveSession();
            JcrTools tools = new JcrTools();
            tools.setDebug(true);
            try {
                tools.printSubgraph(session.getRootNode().getNode("metadata"));
            } catch (RepositoryException e) {
                e.printStackTrace();
            }*/
        }, MetadataAccess.SERVICE);
    }

    @Test
    public void testCreateProject2() throws Exception {
        metadata.commit(() -> {
            Project pj1 = this.projProvider.ensureProject("Project2");
            assertThat(pj1).isNotNull();

            pj1.setSystemName("ProjectName2");
            pj1.setProjectName("Project Name 2");
            pj1.setDescription("This is a fully defined project");
            pj1.setContainerImage("kylo/nonExistentContainer");
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testCreateProject2")
    public void testFindProjectByName() {
        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName("Project2");

            assertThat(optional.isPresent()).isTrue();

            Project project = optional.get();

            assertThat(project).extracting(Project::getSystemName, Project::getProjectName,
                                           Project::getDescription, Project::getContainerImage)
                .containsExactly("ProjectName2", "Project Name 2", "This is a fully defined project", "kylo/nonExistentContainer");
        }, MetadataAccess.SERVICE);
    }

    @Test(dependsOnMethods = "testFindProjectByName")
    public void testCannotFindProjectByName() {
        metadata.read(() -> {
            Optional<Project> optional = projProvider.findProjectByName("bogus");

            assertThat(optional.isPresent()).isFalse();
        }, MetadataAccess.SERVICE);
    }


    @AfterClass
    public void afterClass() {
        metadata.commit(() -> {
            Collection<Project> projects = projProvider.getProjects();
            for (Project project : projects) {
                logger.info("Deleting remaing project '{}' after tests completed.", project);
                projProvider.deleteProject(project);
            }
        }, MetadataAccess.SERVICE);
    }

}
