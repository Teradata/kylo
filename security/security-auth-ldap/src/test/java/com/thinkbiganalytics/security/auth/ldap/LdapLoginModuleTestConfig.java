/**
 *
 */
package com.thinkbiganalytics.security.auth.ldap;

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

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 *
 */
@Configuration
@EnableAutoConfiguration
public class LdapLoginModuleTestConfig {

    //    private static final String LDAP_ROOT = "ou=schema,dc=example,dc=com";
    private static final String LDAP_ROOT = "dc=example,dc=com";
    private static final int LDAP_PORT = 52389;


    @Bean
    public ApacheDSContainer dsContainer() throws Exception {
        ApacheDSContainer server = new ApacheDSContainer(LDAP_ROOT, "classpath:test-server.ldif");
        server.setPort(LDAP_PORT);
        server.afterPropertiesSet();
        return server;
    }

    @Bean(name = "servicesLdapLoginConfiguration")
    @Primary
    public LoginConfiguration servicesLdapLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
    }

    @Bean(name = "uiLdapLoginConfiguration")
    public LoginConfiguration uiLdapLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
    }

}
