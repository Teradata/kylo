/**
 * 
 */
package com.thinkbiganalytics.security.action;

/**
 *
 * @author Sean Felten
 */
public interface ApplicationAllowedActionsProvider {

    AllowedActions getAllowedActions(String appName);
    
    void checkPermission(String appName, AllowableAction action);
}
