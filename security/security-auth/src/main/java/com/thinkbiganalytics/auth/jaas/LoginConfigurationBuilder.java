/**
 * 
 */
package com.thinkbiganalytics.auth.jaas;

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.spi.LoginModule;

/**
 *
 * @author Sean Felten
 */
public interface LoginConfigurationBuilder {

    ModuleBuilder loginModule(String appName);
    
    LoginConfiguration build();
    
    
    interface ModuleBuilder {
        
        ModuleBuilder moduleClass(Class<? extends LoginModule> moduleClass);
        ModuleBuilder controlFlag(LoginModuleControlFlag flag);
        ModuleBuilder option(String name, Object value);
        
        LoginConfigurationBuilder add();
    }
}
