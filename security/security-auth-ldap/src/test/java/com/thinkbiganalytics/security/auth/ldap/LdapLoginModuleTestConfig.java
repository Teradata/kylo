/**
 * 
 */
package com.thinkbiganalytics.security.auth.ldap;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.ldap.server.ApacheDSContainer;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;

/**
 *
 * @author Sean Felten
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
