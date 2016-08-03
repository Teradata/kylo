/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.spi.LoginModule;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;

/**
 * Default implementation of LoginConfigurationBuilder.
 * @author Sean Felten
 */
public class DefaultLoginConfigurationBuilder implements LoginConfigurationBuilder {

    private DefaultLoginConfiguration configuration = new DefaultLoginConfiguration();
    
    public DefaultLoginConfigurationBuilder() {
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder#loginModule(java.lang.String)
     */
    @Override
    public DefaultModuleBuilder loginModule(String appName) {
        return new DefaultModuleBuilder(this, appName);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder#build()
     */
    @Override
    public LoginConfiguration build() {
        return this.configuration;
    }

    protected void addEntry(String appName, AppConfigurationEntry configEntries) {
        this.configuration.addEntry(appName, configEntries);
    }
    
    public class DefaultModuleBuilder implements ModuleBuilder {
        
        private String appName;
        private Class<? extends LoginModule> moduleClass;
        private LoginModuleControlFlag flag;
        private Map<String, Object> options = new HashMap<>();
        private DefaultLoginConfigurationBuilder confBuilder;

        public DefaultModuleBuilder(DefaultLoginConfigurationBuilder parent, String appName) {
            this.appName = appName;
            this.confBuilder = parent;
        }

        @Override
        public ModuleBuilder moduleClass(Class<? extends LoginModule> moduleClass) {
            this.moduleClass = moduleClass;
            return this;
        }

        @Override
        public ModuleBuilder controlFlag(LoginModuleControlFlag flag) {
            this.flag = flag;
            return this;
        }

        @Override
        public ModuleBuilder option(String name, Object value) {
            this.options.put(name, value);
            return this;
        }
        
        @Override
        public ModuleBuilder options(Map<String, Object> options) {
            this.options.putAll(options);
            return this;
        }

        @Override
        public LoginConfigurationBuilder add() {
            AppConfigurationEntry entry = new AppConfigurationEntry(this.moduleClass.getName(), this.flag, this.options);
            confBuilder.addEntry(this.appName, entry);
            return confBuilder;
        }
    }
    
    public class DefaultLoginConfiguration implements LoginConfiguration {

        private Map<String, List<AppConfigurationEntry>> configEntries = new HashMap<>();

        @Override
        public AppConfigurationEntry[] getApplicationEntries(String appName) {
            List<AppConfigurationEntry> list = this.configEntries.get(appName);
            return list != null ? list.toArray(new AppConfigurationEntry[list.size()]) : new AppConfigurationEntry[0];
        }
        
        @Override
        public Map<String, AppConfigurationEntry[]> getAllApplicationEntries() {
            return this.configEntries.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey(), 
                                                      e -> e.getValue().toArray(new AppConfigurationEntry[e.getValue().size()])));
        }
        
        protected void addEntry(String appName, AppConfigurationEntry entry) {
            List<AppConfigurationEntry> list = this.configEntries.get(appName);
            
            if (list == null) {
                list = new ArrayList<>();
                this.configEntries.put(appName, list);
            }
            
            list.add(entry);
        }
    }
}
