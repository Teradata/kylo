package com.thinkbiganalytics.metadata.modeshape.project;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.thinkbiganalytics.metadata.api.project.security.ProjectAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.project.providers.ProjectProvider;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.security.AccessControlException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class,
                                 JcrProjectProviderAccessControlTest.TestProjectServiceConfig.class}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource
public class JcrProjectProviderAccessControlTest extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LoggerFactory.getLogger(JcrProjectProviderAccessControlTest.class);
    private static final UsernamePrincipal TEST_USER1 = new UsernamePrincipal("tester1");
    private static final UsernamePrincipal TEST_USER2 = new UsernamePrincipal("tester2");
    private static final UsernamePrincipal TEST_USER3 = new UsernamePrincipal("tester3");

    @Inject
    private JcrMetadataAccess metadata;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Resource
    private ProjectProvider projProvider;

    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private ActionsModuleBuilder builder;

    @BeforeClass
    public void beforeClass() {
        metadata.commit(() -> builder
            .module(AllowedActions.PROJECTS)
            .action(ProjectAccessControl.EDIT_PROJECT)
            .action(ProjectAccessControl.ACCESS_PROJECT)
            .action(ProjectAccessControl.CHANGE_PERMS)
            .action(ProjectAccessControl.DELETE_PROJECT)
            .add()
            .build(), MetadataAccess.SERVICE);
    }

    @Test
    public void testCreateProject3() {
        logger.info("Running Test 'testCreateProject3'" );
// taken from: JcrFeedAllowedActionsTest createFeeds()

        String projectName = metadata.commit(() -> {
/*
            actionsProvider.getAllowedActions(AllowedActions.PROJECTS).ifPresent(allowed -> allowed.enableAll(TEST_USER1));
            actionsProvider.getAllowedActions(AllowedActions.PROJECTS).ifPresent(allowed -> allowed.enableAll(TEST_USER2));

            this.roleProvider.createRole(SecurityRole.PROJECT, "testEditor", "Editor", "Can edit projects")
                .setPermissions(ProjectAccessControl.EDIT_PROJECT, FeedAccessControl.ENABLE_DISABLE, FeedAccessControl.EXPORT);
            this.roleProvider.createRole(SecurityRole.PROJECT, "testViewer", "Viewer", "Can view projects only")
                .setPermissions(ProjectAccessControl.ACCESS_PROJECT);
                */

            Project p3 = projProvider.ensureProject("Project3");
            p3.setProjectName("ProjectName3");
            p3.setContainerImage("kylo/nonExisentImage");

            // sets Access Control for user.
            //p3.getAllowedActions().enableAll(TEST_USER1);
            //p3.getAllowedActions().enable(TEST_USER2, ProjectAccessControl.ACCESS_PROJECT);

            // add User to the Role (new way of doing it)
            p3.getRoleMembership(ProjectAccessControl.ROLE_EDITOR).ifPresent(role -> role.addMember(TEST_USER1));
            p3.getRoleMembership(ProjectAccessControl.ROLE_READER).ifPresent(role -> role.addMember(TEST_USER2));

            return p3.getProjectName();
        }, JcrMetadataAccess.SERVICE);

    }

    @Test(dependsOnMethods = "testCreateProject3", expectedExceptions = AccessControlException.class)
    public void checkPermsDeniedProject3() {
        logger.info("Running Test 'checkPermsDeniedProject3'" );
        metadata.read(() -> {
                          Optional<Project> u1Proj = this.projProvider.findProjectByName("Project3");
                          assertThat(u1Proj.isPresent()).isTrue();
                          Project Project3 = u1Proj.get();
                          Project3.getAllowedActions().checkPermission(ProjectAccessControl.EDIT_PROJECT);
                      }
            , TEST_USER2
        );
    }

    @Test(dependsOnMethods = "testCreateProject3")
    public void checkPermsAllowedProject3() {
        logger.info("Running Test 'checkPermsAllowedProject3'" );

        metadata.read(() -> {
                          Optional<Project> u1Proj = this.projProvider.findProjectByName("Project3");
                          assertThat(u1Proj.isPresent()).isTrue();
                          Project Project3 = u1Proj.get();
                          Project3.getAllowedActions().checkPermission(ProjectAccessControl.EDIT_PROJECT);
                      }
            , TEST_USER1
        );
    }

    @Test(dependsOnMethods = "testCreateProject3")
    public void checkNotFoundProject3() {
        logger.info("Running Test 'checkNotFoundProject3'" );

        metadata.read(() -> {
                          Optional<Project> u1Proj = this.projProvider.findProjectByName("Project3");
                          assertThat(u1Proj.isPresent()).isFalse();
                      }
            , TEST_USER3
        );
    }

    // TODO: this test fails on Jenkins but runs local
    @Test(dependsOnMethods = "testCreateProject3", enabled = false)
    public void getProjects2() {
        logger.info("Running Test 'getProjects2'" );

        int projectCount = metadata.read(() -> this.projProvider.getProjects().size(), TEST_USER1);
        assertThat(projectCount).isEqualTo(1);
        projectCount = metadata.read(() -> this.projProvider.getProjects().size(), TEST_USER2);
        assertThat(projectCount).isEqualTo(1);
        projectCount = metadata.read(() -> this.projProvider.getProjects().size(), TEST_USER3);
        assertThat(projectCount).isEqualTo(0);
    }

    @Test(dependsOnMethods = "testCreateProject3")
    public void getProjectsIcanEdit() {
        logger.info("Running Test 'getProjectsIcanEdit'" );

        List<Project> projects = metadata.read(
            () -> this.projProvider.getMyEditableProjects(), TEST_USER1);
        assertThat(projects.size()).isEqualTo(1);

        projects = metadata.read(
            () -> this.projProvider.getMyEditableProjects(), TEST_USER2);
        assertThat(projects.size()).isEqualTo(0);

        projects = metadata.read(
            () -> this.projProvider.getMyEditableProjects(), TEST_USER3);
        assertThat(projects.size()).isEqualTo(0);
    }

    @Test(dependsOnMethods = "testCreateProject3")
    public void testUserGroups() {
        logger.info("Running Test 'testUserGroups'" );

        metadata.read(() -> {
                          Optional<Project> u1Proj = this.projProvider.findProjectByName("Project3");
                          assertThat(u1Proj.isPresent()).isTrue();
                          Set<UsernamePrincipal> editors = this.projProvider.getProjectEditors(u1Proj.get());
                          assertThat(editors).hasSize(1).contains(TEST_USER1);

                          Set<UsernamePrincipal> readers = this.projProvider.getProjectReaders(u1Proj.get());
                          assertThat(readers).hasSize(1).contains(TEST_USER2);
                      }
            , TEST_USER1);

    }

    @Configuration
    static class TestProjectServiceConfig {

        @Bean
        public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
            final Properties properties = new Properties();
            properties.setProperty("security.entity.access.controlled", "true");

            final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            configurer.setProperties(properties);
            return configurer;
        }

    }

}
