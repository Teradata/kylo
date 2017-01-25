/**
 * 
 */
package com.thinkbiganalytics.security.auth.kerberos;

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
