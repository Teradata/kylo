package com.thinkbiganalytics.security.service.user;

/*-
 * #%L
 * thinkbig-security-controller
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
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.rest.model.User;
import com.thinkbiganalytics.security.rest.model.UserGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Provides access to Kylo users and groups stored in the metadata store.
 */
public class UserMetadataService implements UserService {

    /**
     * Metadata access provider
     */
    @Inject
    private MetadataAccess metadataAccess;

    /**
     * Metadata users and groups provider
     */
    @Inject
    private UserProvider userProvider;
    
    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    /**
     * Access controller for permission checks
     */
    @Inject
    private AccessController accessController;

    @Override
    public boolean deleteGroup(@Nonnull final String groupId) {
        Optional<GroupPrincipal> principal = metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_GROUPS);

            return userProvider.findGroupByName(groupId)
                .map(group -> {
                    GroupPrincipal princ = group.getPrincial();
                    userProvider.deleteGroup(group);
                    return princ;
                });
        });

        // Disable all privileges for the deleted group.  This currently must be run as the 
        // privileged service principal to allow access control changes on all service actions.
        if (principal.isPresent()) {
            return metadataAccess.commit(() -> {
                actionsProvider.getAllowedActions(AllowedActions.SERVICES) 
                    .ifPresent(allowed -> allowed.disableAll(principal.get()));
                return true;
            }, MetadataAccess.SERVICE);
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteUser(@Nonnull final String userId) {
        return metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_USERS);

            userProvider.findUserBySystemName(userId)
                .ifPresent(userProvider::deleteUser);
            return true;
        });
    }

    @Nonnull
    @Override
    public Optional<UserGroup> getGroup(@Nonnull final String groupId) {
        return metadataAccess.read(() -> {
         //   accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS);

            return userProvider.findGroupByName(groupId)
                .map(UserModelTransform.toGroupPrincipal());
        });
    }

    @Nonnull
    @Override
    public List<UserGroup> getGroups() {
        return metadataAccess.read(() -> {
          //  accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS);

            return StreamSupport.stream(userProvider.findGroups().spliterator(), false)
                .map(UserModelTransform.toGroupPrincipal())
                .collect(Collectors.toList());
        });
    }

    @Nonnull
    @Override
    public Optional<User> getUser(@Nonnull final String userId) {
        return metadataAccess.read(() -> {
          //  accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_USERS);

            return userProvider.findUserBySystemName(userId)
                .map(UserModelTransform.toUserPrincipal());
        });
    }

    @Nonnull
    @Override
    public List<User> getUsers() {
        return metadataAccess.read(() -> {
         //   accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_USERS);

            return StreamSupport.stream(userProvider.findUsers().spliterator(), false)
                .map(UserModelTransform.toUserPrincipal())
                .collect(Collectors.toList());
        });
    }

    @Nonnull
    @Override
    public Optional<List<User>> getUsersByGroup(@Nonnull final String groupId) {
        return metadataAccess.read(() -> {
        //    accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS, UsersGroupsAccessContol.ACCESS_USERS);

            return userProvider.findGroupByName(groupId)
                .map(users -> StreamSupport.stream(users.getUsers().spliterator(), false)
                    .map(UserModelTransform.toUserPrincipal())
                    .collect(Collectors.toList()));
        });
    }

    @Override
    public void updateGroup(@Nonnull final UserGroup group) {
        metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_GROUPS);

            final com.thinkbiganalytics.metadata.api.user.UserGroup domain = userProvider.findGroupByName(group.getSystemName())
                .orElseGet(() -> userProvider.createGroup(group.getSystemName()));
            domain.setDescription(group.getDescription());
            domain.setTitle(group.getTitle());
            return userProvider.updateGroup(domain);
        });
    }

    @Override
    public void updateUser(@Nonnull final User user) {
        metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_USERS);

            final com.thinkbiganalytics.metadata.api.user.User domain = userProvider.findUserBySystemName(user.getSystemName())
                .orElseGet(() -> userProvider.createUser(user.getSystemName()));
            domain.setDisplayName(user.getDisplayName());
            domain.setEmail(user.getEmail());
            domain.setEnabled(user.isEnabled());

            final Set<com.thinkbiganalytics.metadata.api.user.UserGroup> groups = user.getGroups().stream()
                .map(groupName -> userProvider.findGroupByName(groupName).get())
                .collect(Collectors.toSet());
            domain.setGroups(groups);

            return userProvider.updateUser(domain);
        });
    }
}
