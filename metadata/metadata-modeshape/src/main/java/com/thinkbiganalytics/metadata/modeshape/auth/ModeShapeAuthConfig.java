/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.auth;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class ModeShapeAuthConfig {
    
    @Bean
    public AuthorityGranter modeShapeAuthorityGranter()  {
        return new ModeShapeAuthorityGranter();
    }

    @Bean(name = "servicesModeShapeLoginConfiguration")
    public LoginConfiguration servicesModeShapeLoginConfiguration(LoginConfigurationBuilder builder) {
        return builder
                        .loginModule(JaasAuthConfig.JAAS_SERVICES)
                            .moduleClass(ModeShapeLoginModule.class)
                            .controlFlag(LoginModuleControlFlag.REQUIRED)
                            .add()
                        .build();
    }

}
