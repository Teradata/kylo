/**
 * 
 */
package com.thinkbiganalytics.security.service.role;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public interface SecurityRoleService {

    Optional<SecurityRole> getEntityRole(String entityName, String roleName);
    
    List<SecurityRole> getEntityRoles(String entityName);
    
    Map<String, List<SecurityRole>> getAllEntityRoles();
    
    SecurityRole createRole(String entityName, String systemName, String title, String descr);
    
    
}
