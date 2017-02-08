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

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 *
 */
@Configuration
@EnableAutoConfiguration
public class ActiveDirectoryLoginModuleTestConfig {

    @Primary
    @Bean(name = "servicesActiveDirectoryLoginConfiguration")
    public LoginConfiguration servicesLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
    }

    @Primary
    @Bean(name = "uiActiveDirectoryLoginConfiguration")
    public LoginConfiguration uiLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
    }

}
