/**
 * 
 */
package com.thinkbiganalytics.security.action;

/**
 *
 * @author Sean Felten
 */
public interface AllowedActionsGroupProvider {

    AllowedActions getAllowedActions(String appName);
    
    void checkPermission(String groupName, AllowableAction action);
}
