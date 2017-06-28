package com.thinkbiganalytics.security.auth.kerberos;

/*-
 * #%L
 * thinkbig-security-auth-kerberos
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * This configuration enables the components used in kerberos-based username/password authentication.
 */
@Configuration
@EnableWebSecurity
@Profile("auth-krb-login")
public class KerberosLoginConfiguration {

    @Value("${security.auth.krb.login.flag:required}")
    private String loginFlag;
    
    @Value("${security.auth.krb.login.order:#{T(com.thinkbiganalytics.auth.jaas.LoginConfiguration).DEFAULT_ORDER}}")
    private int loginOrder;

    @Bean(name = "krbLoginConfiguration")
    @SuppressWarnings("restriction")
    public LoginConfiguration servicesLdapLoginConfiguration(LoginConfigurationBuilder builder) {
        // @formatter:off

        return builder
                .order(this.loginOrder)
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(com.sun.security.auth.module.Krb5LoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option("storeKey", "true")
                    .add()
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(com.sun.security.auth.module.Krb5LoginModule.class)
                    .controlFlag(this.loginFlag)
                    .option("storeKey", "true")
                    .add()
                .build();

        // @formatter:on
    }
}
