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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.api.project.security.ProjectAccessControl;
import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.project.JcrProject;
import com.thinkbiganalytics.metadata.modeshape.project.ProjectPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class JcrProjectProvider extends BaseJcrProvider<Project, Project.ID> implements ProjectProvider {

    public static Logger logger = LoggerFactory.getLogger(JcrProjectProvider.class);


    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Inject
    private AccessController accessController;

    @Inject
    private SecurityRoleProvider roleProvider;

    @Override
    protected String getEntityQueryStartingPath() {
        return "/" + ProjectPaths.PROJECTS.toString();
    }

    @Nonnull
    @Override
    public Optional<Project> findProjectByName(@Nonnull String name) {
        String query = "SELECT * FROM [tba:project] AS Projects WHERE LOCALNAME() = $name ";
        query = applyFindAllFilter(query, "/" + ProjectPaths.PROJECTS.toString());
        final Map<String, String> bindParams = Collections.singletonMap("name", name);
        return Optional.ofNullable(JcrQueryUtil.findFirst(getSession(), query, bindParams, getEntityClass()));
    }


    @Nonnull
    @Override
    public Project createProject(@Nonnull String path) {
        return createProject(path, false);
    }

    @Nonnull
    @Override
    public Project ensureProject(@Nonnull String path) {
        return createProject(path, true);
    }


    /**
     * Creates a new user with the specified name.
     *
     * @param name   the name of the Project
     * @param ensure {@code true} to return the Project if it already exists, or {@code false} to throw an exception
     * @return the Project
     * @throws MetadataRepositoryException if the user could not be created
     */
    @Nonnull
    private Project createProject(@Nonnull final String name, final boolean ensure) {
        final Session session = getSession();
        final String projPath = ProjectPaths.projectPath(name).toString();

        logger.debug("workspace= {}", session.getWorkspace().getName());

        try {
            Node projNode = session.getRootNode().getNode(ProjectPaths.PROJECTS.toString());

            if (session.getRootNode().hasNode(projPath)) {
                if (ensure) {
                    return JcrUtil.getJcrObject(projNode, name, JcrProject.class);
                } else {
                    // TODO specialize me..
                    throw new RuntimeException(projPath);
                }
            } else {
                // project does not yet exist
                JcrProject newProject = JcrUtil.getOrCreateNode(projNode, name, JcrProject.NODE_TYPE, JcrProject.class);

                // grant (or deny) current user access to the project he is creating
                if (this.accessController.isEntityAccessControlled()) {
                    List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.PROJECT);
                    this.actionsProvider.getAvailableActions(AllowedActions.PROJECTS)
                        .ifPresent(actions -> newProject.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
                } else {
                    this.actionsProvider.getAvailableActions(AllowedActions.PROJECTS)
                        .ifPresent(actions -> newProject.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
                }

                return newProject;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed attempting to create a new Project with name: " + name, e);
        }
    }


    @Override
    public Class<? extends Project> getEntityClass() {
        return JcrProject.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrProject.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrProject.NODE_TYPE;
    }


    @Override
    public Project.ID resolveId(Serializable fid) {
        return new JcrProject.ProjectId(fid);
    }

    @Override
    public List<Project> getProjects() {
        return findAll();
    }

    @Override
    public List<Project> getMyEditableProjects() {
        UsernamePrincipal user = JcrMetadataAccess.getActiveUser();
        logger.debug("user={}", user);

        List<Project> projects = getProjects();
        return projects.stream()
            .filter(prj ->
                    {
                        try {
                            prj.getAllowedActions().checkPermission(ProjectAccessControl.EDIT_PROJECT);
                            return true;
                        } catch (AccessControlException ace) {
                            return false;
                        }
                    }).collect(Collectors.toList());
    }

    @Override
    public void deleteProject(Project domain) {
        if (accessController.isEntityAccessControlled()) {
            domain.getAllowedActions().checkPermission(ProjectAccessControl.DELETE_PROJECT);
        }

        super.delete(domain);
    }

    @Override
    public Set<UsernamePrincipal> getProjectMembersWithRoleById(String id, String rolename) {
        Set<UsernamePrincipal> users = Sets.newHashSet();

        Project project = findById(new JcrProject.ProjectId(id));

        return getProjectMembersWithRole( project, rolename);
    }

    private Set<UsernamePrincipal> getProjectMembersWithRole(Project domain, String roleName) {
        Set<UsernamePrincipal> users = Sets.newHashSet();

        Optional<RoleMembership> roleMembership = domain.getRoleMembership(roleName);
        roleMembership.ifPresent(membership -> membership.getMembers().stream().
            filter(member -> member instanceof UsernamePrincipal).
            forEach(member -> {
                users.add((UsernamePrincipal) member);
            }));

        return users;
    }

    @Override
    public Set<UsernamePrincipal> getProjectUsers(Project domain) {
        Set<UsernamePrincipal> users = Sets.newHashSet();

        Set<RoleMembership> roleMembership = domain.getRoleMemberships();
        roleMembership.forEach(membership -> membership.getMembers().stream().
            filter(member -> member instanceof UsernamePrincipal).
            forEach(member -> {
                users.add((UsernamePrincipal) member);
            }));

        return users;
    }

    @Override
    public Set<UsernamePrincipal> getProjectEditors(Project domain) {
        return getProjectMembersWithRole(domain, ProjectAccessControl.ROLE_EDITOR);
    }

    @Override
    public Set<UsernamePrincipal> getProjectReaders(Project domain) {
        return getProjectMembersWithRole(domain, ProjectAccessControl.ROLE_READER);
    }

    @Override
    public Set<UsernamePrincipal> getProjectOwnerAndReaders(String systemName) {
        Set<UsernamePrincipal> users = Sets.newHashSet();

        users.addAll(
            getProjectOwnerPlusRole(systemName,ProjectAccessControl.ROLE_READER) );
        users.addAll(
            getProjectOwnerPlusRole(systemName,ProjectAccessControl.ROLE_EDITOR) );
        users.addAll(
            getProjectOwnerPlusRole(systemName,ProjectAccessControl.ROLE_ADMIN) );

        return users;
    }

    @Override
    public Set<UsernamePrincipal> getProjectOwnerAndEditors(String systemName) {
        Set<UsernamePrincipal> users = Sets.newHashSet();

        users.addAll(
            getProjectOwnerPlusRole(systemName,ProjectAccessControl.ROLE_EDITOR) );
        users.addAll(
            getProjectOwnerPlusRole(systemName,ProjectAccessControl.ROLE_ADMIN) );

        return users;
    }

    private Set<UsernamePrincipal> getProjectOwnerPlusRole(String systemName, String accessControl) {
        Optional<Project> proj = findProjectByName(systemName);

        if( proj.isPresent() ) {
            Project domain = proj.get();
            Set<UsernamePrincipal> result =  getProjectMembersWithRole(domain, accessControl);
            result.add((UsernamePrincipal)domain.getOwner());  // Owners can read...
            return result;
        } else {
            return ImmutableSet.of();
        }
    }



}
