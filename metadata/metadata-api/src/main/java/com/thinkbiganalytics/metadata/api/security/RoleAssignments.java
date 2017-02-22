/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import java.security.Principal;
import java.util.Set;

import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public interface RoleAssignments {

    Set<SecurityRole> getAssignedRoles();
    
    Set<Principal> getMembers(SecurityRole role);
    
    void addMember(SecurityRole role, UsernamePrincipal principal);
    
    void addMember(SecurityRole role, GroupPrincipal principal);
    
    void removeMember(SecurityRole role, UsernamePrincipal principal);
    
    void removeMember(SecurityRole role, GroupPrincipal principal);
}
