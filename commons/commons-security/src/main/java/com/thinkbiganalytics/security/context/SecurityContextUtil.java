/**
 * 
 */
package com.thinkbiganalytics.security.context;

/*-
 * #%L
 * kylo-commons-security
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.security.AnonymousPrincipal;
import com.thinkbiganalytics.security.SimplePrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for retrieving principals and user information from the active security
 * context at the time.
 */
public class SecurityContextUtil {

    public static Set<Principal> getCurrentPrincipals() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<Principal> principals = new HashSet<>();
        
        if (auth != null) {
            for (GrantedAuthority grant : auth.getAuthorities()) {
                if (grant instanceof JaasGrantedAuthority) {
                    JaasGrantedAuthority jaasGrant = (JaasGrantedAuthority) grant;
                    principals.add(jaasGrant.getPrincipal());
                } else {
                    String authority = grant.getAuthority();
                    
                    if (authority != null) {
                        principals.add(new SimplePrincipal(authority));
                    }
                }
            }
            
            principals.add(new UsernamePrincipal(auth.getName()));
        } else {
            principals.add(new AnonymousPrincipal());
        }
        
        return principals;
    }
    
    public static UsernamePrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            return new UsernamePrincipal(auth.getName());
        } else {
            return new AnonymousPrincipal();
        }
    }
    
    public static String getCurrentUsername() {
        return getCurrentUserPrincipal().getName();
    }
    
    private SecurityContextUtil() {
    }
}
