package com.thinkbiganalytics.security.auth.kerberos;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

/**
 * LDAP login configuration.
 * 
 * @author Sean Felten
 */
//@Configuration
//@Profile("auth-ldap")
public class KerberosLoginModuleConfiguration {
    
    @Value("${security.auth.krb.login.ui:required}")
    private String uiLoginFlag;

    @Bean(name = "uiKrbLoginConfiguration")
    @SuppressWarnings("restriction")
    public LoginConfiguration servicesLdapLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(com.sun.security.auth.module.Krb5LoginModule.class)
                    .controlFlag(this.uiLoginFlag)
                    .option("storeKey", "true")
                    .add()
                .build();
    }
}
