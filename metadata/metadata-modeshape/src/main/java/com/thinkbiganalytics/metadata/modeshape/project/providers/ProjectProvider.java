package com.thinkbiganalytics.metadata.modeshape.project.providers;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public interface ProjectProvider extends BaseProvider<Project, Project.ID> {

    /**
     * Finds the project based on name alone
     *
     * @return optionally a single notebook
     * @throws exception when the name is not unique in the project tree
     */
    @Nonnull
    Optional<Project> findProjectByName(@Nonnull String name);

    /**
     * Creates the notebook with the specified name (relative to the root).
     *
     * @param name the path for storing the project
     * @return the project
     */
    Project createProject(@Nonnull String name);


    /**
     * Gets or creates the notebook with the specified name (relative to the root).
     *
     * @param name the path for storing the project
     * @return the project
     */
    @Nonnull
    Project ensureProject(@Nonnull String name);

    /**
     * Gets the list of all projects in the system
     */
    List<Project> getProjects();

    /**
     * Gets the list of all projects that a user has edit access to
     */
    List<Project> getMyEditableProjects();

    /**
     * Given a certain project, try to determine all users with any privileges to it
     */
    Set<UsernamePrincipal> getProjectUsers(Project domain);


    /**
     * Given a certain project, try to determine all users with edit privileges to it
     */
    Set<UsernamePrincipal> getProjectEditors(Project domain);


    /**
     * Given a certain project, try to determine all users with read-only privileges to it
     */
    Set<UsernamePrincipal> getProjectReaders(Project domain);

    /**
     * Delete the project
     */
    void deleteProject(Project domain);

    /**
     *
     */
    Set<UsernamePrincipal> getProjectMembersWithRoleById(String id, String rolename);

    /**
     *
     * @param systemName
     * @return
     */
    Set<UsernamePrincipal> getProjectOwnerAndReaders(String systemName);

    /**
     *
     * @param systemName
     * @return
     */
    Set<UsernamePrincipal> getProjectOwnerAndEditors(String systemName);
}