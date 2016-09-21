/**
 * 
 */
package com.thinkbiganalytics.security.auth.ad;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;

/**
 *
 * @author Sean Felten
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
