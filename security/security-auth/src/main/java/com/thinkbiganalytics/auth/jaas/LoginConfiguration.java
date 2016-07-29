/**
 * 
 */
package com.thinkbiganalytics.auth.jaas;

import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

/**
 *
 * @author Sean Felten
 */
public interface LoginConfiguration {

    AppConfigurationEntry[] getApplicationEntries(String appName);
    
    Map<String, AppConfigurationEntry[]> getAllApplicationEntries();
}
