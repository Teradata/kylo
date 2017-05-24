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

import java.security.acl.Group;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Created by ru186002 on 07/04/2017.
 */
public class RoleSetExposingSecurityExpressionRoot extends SecurityExpressionRoot {

    /**
     * Creates a new instance
     *
     * @param authentication the {@link Authentication} to use. Cannot be null.
     */
    public RoleSetExposingSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    
    public Set<String> getGroups() {
        if (authentication != null) {
            // Currently we have more than one AuthenticationProvider registered and not all produce authorities of type JaasGrantedAuthority.
            // For now we must test all authorities (which will include the username) as potential groups since we can't determine what kind each is.
            return authentication.getAuthorities().stream()
                            .map(GrantedAuthority.class::cast)
                            .map(ga -> ga.getAuthority())
                            .filter(a -> ! a.startsWith("ROLE_"))
                            .collect(Collectors.toSet());
//            return authentication.getAuthorities().stream()
//                            .filter(a -> a instanceof JaasGrantedAuthority)
//                            .map(JaasGrantedAuthority.class::cast)
//                            .filter(jga -> jga.getPrincipal() instanceof Group)
//                            .map(jga -> jga.getAuthority())
//                            .filter(a -> ! a.startsWith("ROLE_"))
//                            .collect(Collectors.toSet());
        } else {
            return Collections.singleton("NULL");
        }
    }

    public String getName() {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "";
        }
    }
    
    public Object getUser() {
        return this;
    }
}
