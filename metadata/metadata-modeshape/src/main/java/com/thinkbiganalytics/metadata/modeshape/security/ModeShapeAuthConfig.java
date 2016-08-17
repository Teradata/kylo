/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionGroupsBuilder;
import com.thinkbiganalytics.security.action.config.ActionGroupsBuilder;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class ModeShapeAuthConfig {
    
    private static final Path PROTOTYPES_PATH = Paths.get("metadata", "security", "prototypes");
    
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
    
    @Bean(name = "prototypesActionGroupsBuilder")
    public ActionGroupsBuilder prototypesActionGroupsBuilder() {
        return new JcrActionGroupsBuilder(PROTOTYPES_PATH.toString());
    }

}
