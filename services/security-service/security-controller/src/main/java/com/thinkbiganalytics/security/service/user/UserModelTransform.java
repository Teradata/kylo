package com.thinkbiganalytics.security.service.user;

import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.security.rest.model.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.UserPrincipal;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Transforms users and groups between Security and Metadata objects.
 */
public class UserModelTransform {

    /**
     * Transforms Metadata groups to Security groups.
     *
     * @return the Security groups
     */
    @Nonnull
    public static Function<UserGroup, GroupPrincipal> toGroupPrincipal() {
        return (UserGroup group) -> {
            final GroupPrincipal principal = new GroupPrincipal();
            principal.setDescription(group.getDescription());
            principal.setMemberCount(Iterables.size(group.getGroups()) + Iterables.size(group.getUsers()));
            principal.setTitle(group.getTitle());
            principal.setSystemName(group.getSystemName());
            return principal;
        };
    }

    /**
     * Transforms Metadata users to Security users.
     *
     * @return the Security users
     */
    @Nonnull
    public static Function<User, UserPrincipal> toUserPrincipal() {
        return (User user) -> {
            final UserPrincipal principal = new UserPrincipal();
            principal.setDisplayName(user.getDisplayName());
            principal.setEmail(user.getEmail());
            principal.setEnabled(user.isEnabled());
            principal.setGroups(user.getGroups().stream()
                                        .map(UserGroup::getSystemName)
                                        .collect(Collectors.toSet()));
            principal.setSystemName(user.getSystemName());
            return principal;
        };
    }

    /**
     * Instances of {@code UserModelTransform} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private UserModelTransform() {
        throw new UnsupportedOperationException();
    }
}
