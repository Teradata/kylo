package com.thinkbiganalytics.metadata.config;

/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ru186002 on 07/04/2017.
 */
public class RoleSetExposingSecurityExpressionRoot extends SecurityExpressionRoot {

    private Set<String> roles;
    private RoleHierarchy roleHierarchy;

    /**
     * Creates a new instance
     *
     * @param authentication the {@link Authentication} to use. Cannot be null.
     */
    public RoleSetExposingSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    @Override
    public void setRoleHierarchy(RoleHierarchy roleHierarchy) {
        super.setRoleHierarchy(roleHierarchy);
        this.roleHierarchy = roleHierarchy;
    }

    @SuppressWarnings("unused") //this method is used dynamically in @Query annotations, e.g. "... and x.principalName in :#{principal.roleSet}"
    public Set<String> getRoleSet() {
        if (roles == null) {
            roles = new HashSet<>();
            Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();

            if (roleHierarchy != null) {
                userAuthorities = roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
            }

            roles = AuthorityUtils.authorityListToSet(userAuthorities);
        }

        return roles;
    }

}
