/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

/*-
 * #%L
 * thinkbig-security-auth
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;

import com.thinkbiganalytics.auth.DefaultPrincipalAuthorityGranter;
import com.thinkbiganalytics.auth.RolePrincipalAuthorityGranter;
import com.thinkbiganalytics.auth.UserRoleAuthorityGranter;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JaasAuthConfig {

    public static final String JAAS_UI = "UI";
    public static final String JAAS_SERVICES = "Services";
    
    public static final String SERVICES_AUTH_PROVIDER = "servicesAuthenticationProvider";
    public static final String UI_AUTH_PROVIDER = "uiAuthenticationProvider";
    
    public static final int DEFAULT_GRANTER_ORDER = Integer.MAX_VALUE - 100;
    

    @Bean(name = UI_AUTH_PROVIDER)
    public AuthenticationProvider uiAuthenticationProvider(@Named("jaasConfiguration") javax.security.auth.login.Configuration config,
                                                           List<AuthorityGranter> authorityGranters) {
        DefaultJaasAuthenticationProvider provider = new DefaultJaasAuthenticationProvider();
        provider.setConfiguration(config);
        provider.setAuthorityGranters(authorityGranters.toArray(new AuthorityGranter[authorityGranters.size()]));
        provider.setLoginContextName(JAAS_UI);
        return provider;
    }
    
    @Bean(name = SERVICES_AUTH_PROVIDER)
    public AuthenticationProvider servicesAuthenticationProvider(@Named("jaasConfiguration") javax.security.auth.login.Configuration config,
                                                             List<AuthorityGranter> authorityGranters) {
        DefaultJaasAuthenticationProvider provider = new DefaultJaasAuthenticationProvider();
        provider.setConfiguration(config);
        provider.setAuthorityGranters(authorityGranters.toArray(new AuthorityGranter[authorityGranters.size()]));
        provider.setLoginContextName(JAAS_SERVICES);
        return provider;
    }

    @Bean(name="jaasConfiguration")
    public javax.security.auth.login.Configuration jaasConfiguration(List<LoginConfiguration> loginModuleEntries) {
        Map<String, AppConfigurationEntry[]> merged = loginModuleEntries.stream()
            .map(c -> c.getAllApplicationEntries().entrySet())
            .flatMap(s -> s.stream())
            .collect(Collectors.toMap(e -> e.getKey(), 
                                      e -> e.getValue(),
                                      ArrayUtils::addAll));

        return new InMemoryConfiguration(merged);
    }
    
    @Bean(name = "rolePrincipalAuthorityGranter")
    @Order(DEFAULT_GRANTER_ORDER - 100)
    public AuthorityGranter rolePrincipalAuthorityGranter() {
        return new RolePrincipalAuthorityGranter();
    }
    
    @Bean(name = "userRoleAuthorityGranter")
    @Order(DEFAULT_GRANTER_ORDER - 100)
    public AuthorityGranter userRoleAuthorityGranter() {
        return new UserRoleAuthorityGranter();
    }
    
    @Bean(name = "defaultAuthorityGranter")
    @Order(DEFAULT_GRANTER_ORDER)
    public AuthorityGranter defaultAuthorityGranter() {
        return new DefaultPrincipalAuthorityGranter();
    }
    
    @Bean
    @Scope("prototype")
    public LoginConfigurationBuilder loginConfigurationBuilder() {
        return new DefaultLoginConfigurationBuilder();
    }
}
