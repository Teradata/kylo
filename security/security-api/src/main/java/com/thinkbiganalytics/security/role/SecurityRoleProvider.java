/**
 * 
 */
package com.thinkbiganalytics.security.role;

/**
 * Used to manage security roles for entities.
 */
public interface SecurityRoleProvider {

    SecurityRole createRole(String entityName, String roleName, String title, String descr);
    
    SecurityRole getRole(String entityName, String roleName);
    
    boolean removeRole(String entityName, String roleName);
}
