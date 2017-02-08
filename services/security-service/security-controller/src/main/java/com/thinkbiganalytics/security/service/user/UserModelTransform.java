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
     * Instances of {@code UserModelTransform} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private UserModelTransform() {
        throw new UnsupportedOperationException();
    }

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
}
