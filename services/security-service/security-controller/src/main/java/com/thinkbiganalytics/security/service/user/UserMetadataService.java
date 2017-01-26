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
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.rest.model.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.UserPrincipal;

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

    /** Metadata access provider */
    @Inject
    private MetadataAccess metadataAccess;

    /** Metadata users and groups provider */
    @Inject
    private UserProvider userProvider;
    
    /** Access controller for permission checks */
    @Inject
    private AccessController accessController;

    @Override
    public boolean deleteGroup(@Nonnull final String groupId) {
        return metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_GROUPS);
            
            userProvider.findGroupByName(groupId)
                    .ifPresent(userProvider::deleteGroup);
            return true;
        });
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
    public Optional<GroupPrincipal> getGroup(@Nonnull final String groupId) {
        return metadataAccess.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS);
        
            return userProvider.findGroupByName(groupId)
                        .map(UserModelTransform.toGroupPrincipal());
        });
    }

    @Nonnull
    @Override
    public List<GroupPrincipal> getGroups() {
        return metadataAccess.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS);
            
            return StreamSupport.stream(userProvider.findGroups().spliterator(), false)
                        .map(UserModelTransform.toGroupPrincipal())
                        .collect(Collectors.toList());
        });
    }

    @Nonnull
    @Override
    public Optional<UserPrincipal> getUser(@Nonnull final String userId) {
        return metadataAccess.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_USERS);
            
            return userProvider.findUserBySystemName(userId)
                        .map(UserModelTransform.toUserPrincipal());
        });
    }

    @Nonnull
    @Override
    public List<UserPrincipal> getUsers() {
        return metadataAccess.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_USERS);
            
            return StreamSupport.stream(userProvider.findUsers().spliterator(), false)
                        .map(UserModelTransform.toUserPrincipal())
                        .collect(Collectors.toList());
        });
    }

    @Nonnull
    @Override
    public Optional<List<UserPrincipal>> getUsersByGroup(@Nonnull final String groupId) {
        return metadataAccess.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ACCESS_GROUPS, UsersGroupsAccessContol.ACCESS_USERS);
            
            return userProvider.findGroupByName(groupId)
                    .map(users -> StreamSupport.stream(users.getUsers().spliterator(), false)
                         .map(UserModelTransform.toUserPrincipal())
                         .collect(Collectors.toList()));
        });
    }

    @Override
    public void updateGroup(@Nonnull final GroupPrincipal principal) {
        metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_GROUPS);
            
            final UserGroup group = userProvider.findGroupByName(principal.getSystemName())
                    .orElseGet(() -> userProvider.createGroup(principal.getSystemName()));
            group.setDescription(principal.getDescription());
            group.setTitle(principal.getTitle());
            return userProvider.updateGroup(group);
        });
    }

    @Override
    public void updateUser(@Nonnull final UserPrincipal principal) {
        metadataAccess.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, UsersGroupsAccessContol.ADMIN_USERS);
            
            final User user = userProvider.findUserBySystemName(principal.getSystemName())
                    .orElseGet(() -> userProvider.createUser(principal.getSystemName()));
            user.setDisplayName(principal.getDisplayName());
            user.setEmail(principal.getEmail());
            user.setEnabled(principal.isEnabled());

            final Set<UserGroup> groups = principal.getGroups().stream()
                    .map(groupName -> userProvider.findGroupByName(groupName).get())
                    .collect(Collectors.toSet());
            user.setGroups(groups);

            return userProvider.updateUser(user);
        });
    }
}
