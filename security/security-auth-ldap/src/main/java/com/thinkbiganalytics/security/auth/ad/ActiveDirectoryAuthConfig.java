/**
 *
 */
package com.thinkbiganalytics.security.auth.ad;

/*-
 * #%L
 * thinkbig-security-auth-ldap
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

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.net.URI;

/**
 * Active Directory login configuration.
 */
@Configuration
@Profile("auth-ad")
public class ActiveDirectoryAuthConfig {

    @Value("${security.auth.ad.login.ui:required}")
    private String uiLoginFlag;

    @Value("${security.auth.ad.login.services:required}")
    private String servicesLoginFlag;

    @Bean(name = "servicesActiveDirectoryLoginConfiguration")
    public LoginConfiguration servicesAdLoginConfiguration(ActiveDirectoryAuthenticationProvider authProvider,
                                                           UserDetailsContextMapper userMapper,
                                                           LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(ActiveDirectoryLoginModule.class)
                    .controlFlag(this.servicesLoginFlag)
                    .option(ActiveDirectoryLoginModule.AUTH_PROVIDER, authProvider)
                    .add()
                .build();

        // @formatter:on
    }

    @Bean(name = "uiActiveDirectoryLoginConfiguration")
    public LoginConfiguration uiAdLoginConfiguration(ActiveDirectoryAuthenticationProvider authProvider,
                                                     UserDetailsContextMapper userMapper,
                                                     LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(ActiveDirectoryLoginModule.class)
                    .controlFlag(this.uiLoginFlag)
                    .option(ActiveDirectoryLoginModule.AUTH_PROVIDER, authProvider)
                    .add()
                .build();

        // @formatter:on
    }


    @Bean
    @ConfigurationProperties("security.auth.ad.user")
    protected UserDetailsMapperFactory userDetailsContextMapper() {
        return new UserDetailsMapperFactory();
    }

    @Bean
    @ConfigurationProperties("security.auth.ad.server")
    protected ActiveDirectoryProviderFactory activeDirectoryAuthenticationProvider(UserDetailsContextMapper mapper) {
        ActiveDirectoryProviderFactory factory = new ActiveDirectoryProviderFactory();
        factory.setEnableGroups(userDetailsContextMapper().isEnableGroups());  // More consistent to set this with security.auth.ad.user.groupsEnabled=
        factory.setMapper(mapper);
        return factory;
    }


    public static class ActiveDirectoryProviderFactory extends AbstractFactoryBean<ActiveDirectoryAuthenticationProvider> {

        private URI uri;
        private String domain;
        private String searchFilter = null;
        private boolean enableGroups = false;
        private UserDetailsContextMapper mapper;

        public void setEnableGroups(boolean groupsEnabled) {
            this.enableGroups = groupsEnabled;
        }

        public void setUri(String uri) {
            this.uri = URI.create(uri);
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public void setMapper(UserDetailsContextMapper mapper) {
            this.mapper = mapper;
        }
        
        public void setSearchFilter(String searchFilter) {
            this.searchFilter = searchFilter;
        }

        @Override
        public Class<?> getObjectType() {
            return ActiveDirectoryAuthenticationProvider.class;
        }

        @Override
        protected ActiveDirectoryAuthenticationProvider createInstance() throws Exception {
            ActiveDirectoryAuthenticationProvider provider = new ActiveDirectoryAuthenticationProvider(this.domain, 
                                                                                                       this.uri.toASCIIString(), 
                                                                                                       this.enableGroups, 
                                                                                                       null, 
                                                                                                       null);
            provider.setConvertSubErrorCodesToExceptions(true);
            provider.setUserDetailsContextMapper(this.mapper);
            if (this.searchFilter != null) provider.setSearchFilter(this.searchFilter);
            return provider;
        }
    }

    public static class UserDetailsMapperFactory extends AbstractFactoryBean<UserDetailsContextMapper> {

        private boolean enableGroups = false;
        private String[] groupAttributes = null;

        public boolean isEnableGroups() {
            return enableGroups;
        }

        public void setEnableGroups(boolean enabled) {
            this.enableGroups = enabled;
        }

        public void setGroupAttribures(String groupAttribures) {
            this.groupAttributes = groupAttribures.split("\\|");
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Class<?> getObjectType() {
            return UserDetailsContextMapper.class;
        }

        @Override
        protected UserDetailsContextMapper createInstance() throws Exception {
            LdapUserDetailsMapper mapper = new LdapUserDetailsMapper();
            mapper.setConvertToUpperCase(false);
            mapper.setRolePrefix("");
            if (ArrayUtils.isNotEmpty(this.groupAttributes)) {
                mapper.setRoleAttributes(groupAttributes);
            }

            return mapper;
        }
    }
}
