/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActionsGroupProvider;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionsGroupBuilder;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

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
    
    // TODO: Move this to somewhere else more appropriate
    @Bean
    public AccessController accessController() {
        return new DefaultAccessController();
    }
    
    @Bean
    public JcrAllowedActionsGroupProvider allowedModuleActionsProvider() {
        return new JcrAllowedActionsGroupProvider();
    }
    
    @Bean(name = "prototypesActionGroupsBuilder")
    @Scope("prototype")
    public ActionsGroupBuilder prototypesActionGroupsBuilder() {
        return new JcrActionsGroupBuilder(SecurityPaths.PROTOTYPES.toString());
    }

}
