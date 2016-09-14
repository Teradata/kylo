package com.thinkbiganalytics.security.service.user;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
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
    MetadataAccess metadataAccess;

    /** Metadata users and groups provider */
    @Inject
    UserProvider userProvider;

    @Override
    public boolean deleteGroup(@Nonnull final String groupId) {
        return metadataAccess.commit(() -> {
            userProvider.findGroupByName(groupId)
                    .ifPresent(userProvider::deleteGroup);
            return true;
        });
    }

    @Override
    public boolean deleteUser(@Nonnull final String userId) {
        return metadataAccess.commit(() -> {
            userProvider.findUserBySystemName(userId)
                    .ifPresent(userProvider::deleteUser);
            return true;
        });
    }

    @Nonnull
    @Override
    public Optional<GroupPrincipal> getGroup(@Nonnull final String groupId) {
        return metadataAccess.read(() ->
                userProvider.findGroupByName(groupId)
                        .map(UserModelTransform.toGroupPrincipal()));
    }

    @Nonnull
    @Override
    public List<GroupPrincipal> getGroups() {
        return metadataAccess.read(() ->
                StreamSupport.stream(userProvider.findGroups().spliterator(), false)
                        .map(UserModelTransform.toGroupPrincipal())
                        .collect(Collectors.toList())
        );
    }

    @Nonnull
    @Override
    public Optional<UserPrincipal> getUser(@Nonnull final String userId) {
        return metadataAccess.read(() ->
                userProvider.findUserBySystemName(userId)
                        .map(UserModelTransform.toUserPrincipal()));
    }

    @Nonnull
    @Override
    public List<UserPrincipal> getUsers() {
        return metadataAccess.read(() ->
                StreamSupport.stream(userProvider.findUsers().spliterator(), false)
                        .map(UserModelTransform.toUserPrincipal())
                        .collect(Collectors.toList()));
    }

    @Override
    public void updateGroup(@Nonnull final GroupPrincipal principal) {
        metadataAccess.commit(() -> {
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
