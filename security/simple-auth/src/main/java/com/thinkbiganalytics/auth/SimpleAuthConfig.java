package com.thinkbiganalytics.auth;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

/**
 * Generic Auth Service Configuration
 * To Override create a new Configuration Class importing this class
 * Example:
 * @Configuration
 * @Import(SimpleAuthConfig.class)
 * public class MyAuthConfig {
 *     @Bean(name = "authenticationService")
       public  AuthenticationService authenticationService(){
        return new MyAuthService();
        }
 * }
 */
@Configuration
public class SimpleAuthConfig {
    
    @Bean(name = "authenticationService")
    public AuthenticationService authenticationService() {
        return new SimpleAuthenticationService();
    }

    
    @Bean(name = "authServiceConfigurationEntries")
    public Map<String, AppConfigurationEntry[]> restConfigurationEntries() {
        Map<String, AppConfigurationEntry[]> map = new HashMap<>();
        map.put("REST", new AppConfigurationEntry[] { new AppConfigurationEntry(AuthServiceLoginModule.class.getName(), 
                                                                                LoginModuleControlFlag.REQUIRED, 
                                                                                ImmutableMap.of("authService", authenticationService())) });
        return map;
    }
    
    @Bean(name = "testConfigurationEntries")
    public Map<String, AppConfigurationEntry[]> uiConfigurationEntries() {
        Map<String, AppConfigurationEntry[]> map = new HashMap<>();
        map.put("UI", new AppConfigurationEntry[] { new AppConfigurationEntry(AuthServiceLoginModule.class.getName(), 
                                                                              LoginModuleControlFlag.OPTIONAL, 
                                                                              ImmutableMap.of("authService", authenticationService())) });
        return map;
    }
}
