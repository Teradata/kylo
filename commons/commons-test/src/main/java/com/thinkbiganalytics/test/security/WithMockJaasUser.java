/**
 * 
 */
package com.thinkbiganalytics.test.security;

/*-
 * #%L
 * kylo-commons-test
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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.StringUtils;

import com.thinkbiganalytics.security.GroupPrincipal;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMockJaasUser.JaasSecurityContextFactory.class)
@WithMockUser
public @interface WithMockJaasUser {
    @AliasFor(annotation = WithMockUser.class, attribute="value")
    String value() default "user";

    @AliasFor(annotation = WithMockUser.class, attribute="username")
    String username() default "";

    @AliasFor(annotation = WithMockUser.class, attribute="password")
    String password() default "password";
    
    @AliasFor(annotation = WithMockUser.class, attribute="authorities")
    String[] authorities() default {};
    
    @AliasFor(annotation = WithSecurityContext.class, attribute="factory")
    Class<? extends WithSecurityContextFactory<? extends Annotation>> factory() default WithMockJaasUser.JaasSecurityContextFactory.class;

    
    public static class JaasSecurityContextFactory implements WithSecurityContextFactory<WithMockJaasUser> {

        /* (non-Javadoc)
         * @see org.springframework.security.test.context.support.WithSecurityContextFactory#createSecurityContext(java.lang.annotation.Annotation)
         */
        @Override
        public SecurityContext createSecurityContext(WithMockJaasUser withUser) {
            String username = StringUtils.hasLength(withUser.username()) ? withUser.username() : withUser.value();
            List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
            
            for (String authority : withUser.authorities()) {
                grantedAuthorities.add(new JaasGrantedAuthority(authority, new GroupPrincipal(authority)));
            }

            User principal = new User(username, withUser.password(), true, true, true, true, grantedAuthorities);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
        
    }
}
