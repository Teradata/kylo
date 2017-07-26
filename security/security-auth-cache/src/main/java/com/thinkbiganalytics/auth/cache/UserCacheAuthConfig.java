package com.thinkbiganalytics.auth.cache;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

import java.security.Principal;
import java.util.Set;

import javax.inject.Named;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Configures a pair of login modules that short-circuit a login attempt if a user's credentials exist in a cache.
 * Two login modules will be configured:<p><ul>
 * <li>A module that will authenticate a user if the user's credentials still exist in a cache from a prior successful login attempt
 * <li>A module that will store the user's credentials in the cache after a successful login
 * </ul><p>
 * The 1st module is flagged as <i>sufficient</i>, meaning it will immediately authenticate the user if its login is successful
 * without any other login modules participating (short-circuit.)  The 2nd module is flagged as <i>optional</i>, meaning its success or failure will not
 * necessarily prevent login.  This latter module's role is to participate in the commit phase of login and capture and cache
 * any principals loaded by other modules on successful login.
 */
@Configuration
@Profile("auth-cache")
public class UserCacheAuthConfig {
    
    @Value("${security.auth.cache.spec:expireAfterWrite=30s,maximumSize=512}")
    private String cacheSpec;
    
    @Value("${security.auth.file.login.check.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).LOWEST_ORDER}}")
    private int loginCheckOrder;
    
    @Value("${security.auth.file.login.save.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).HIGHEST_ORDER}}")
    private int loginSaveOrder;

    @Bean(name = "loginPrincipalCache")
    public Cache<Principal, Set<Principal>> principalCache() {    
        CacheBuilder<Object, Object> builder = CacheBuilder.from(cacheSpec);
        return builder.build();
    }

    @Bean(name = "servicesCacheAuthenticatingLoginConfiguration")
    public LoginConfiguration servicesCacheAuthenticatingLoginConfiguration(LoginConfigurationBuilder builder,
                                                                            @Named("loginPrincipalCache") Cache<Principal, Set<Principal>> cache) {
        // @formatter:off

        // Caching only is relevant on the services side because the UI already uses cookies
        // to cache authentication state.
        return builder
                        .order(this.loginCheckOrder)
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(UserCacheLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.SUFFICIENT)      
                            .option(UserCacheLoginModule.MODE_OPTION, UserCacheLoginModule.Mode.AUTHENTICATE)
                            .option(UserCacheLoginModule.CACHE_OPTION, cache)
                            .add()
                        .build();

        // @formatter:on
    }
    
    @Bean(name = "servicesCachingLoginConfiguration")
    public LoginConfiguration servicesCachingLoginConfiguration(LoginConfigurationBuilder builder,
                                                                @Named("loginPrincipalCache") Cache<Principal, Set<Principal>> cache) {
        // @formatter:off
        
        // Caching only is relevant on the services side because the UI already uses cookies
        // to cache authentication state.
        return builder
                        .order(this.loginSaveOrder)
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(UserCacheLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.OPTIONAL)      
                            .option(UserCacheLoginModule.MODE_OPTION, UserCacheLoginModule.Mode.CACHE)
                            .option(UserCacheLoginModule.CACHE_OPTION, cache)
                            .add()
                        .build();
        
        // @formatter:on
    }

}
