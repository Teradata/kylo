/**
 * 
 */
package com.thinkbiganalytics.auth.jaas;

import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.spi.LoginModule;

import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder.ModuleBuilder;

/**
 *
 * @author Sean Felten
 */
public interface LoginConfigurationBuilder {

    ModuleBuilder loginModule(String appName);
    
    LoginConfiguration build();
    
    
    interface ModuleBuilder {
        
        ModuleBuilder moduleClass(Class<? extends LoginModule> moduleClass);
        ModuleBuilder controlFlag(String flag);
        ModuleBuilder controlFlag(LoginModuleControlFlag flag);
        ModuleBuilder option(String name, Object value);
        ModuleBuilder options(Map<String, Object> options);
        
        LoginConfigurationBuilder add();
    }
}
