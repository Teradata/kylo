/**
 * 
 */
package com.thinkbiganalytics.security.role;

import java.security.Principal;

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Defines a role used for representing a set of predefined permissions.
 */
public interface SecurityRole {
    
    Principal getPrincipal();
    
    String getSystemName();
    
    String getTitle();
    
    String getDescription();
    
    AllowedActions getAllowedActions();
}
