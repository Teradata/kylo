/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.jaas.AuthorityGranter;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfigurator;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedModuleActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrModuleActionsBuilder;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

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
    
    @Bean
    public JcrAllowedModuleActionsProvider allowedModuleActionsProvider() {
        return new JcrAllowedModuleActionsProvider();
    }
    
    @Bean(name = "prototypesActionGroupsBuilder")
    @Scope("prototype")
    public ModuleActionsBuilder prototypesActionGroupsBuilder(MetadataJcrConfigurator conf) {
        return new JcrModuleActionsBuilder(SecurityPaths.PROTOTYPES.toString());
    }

}
