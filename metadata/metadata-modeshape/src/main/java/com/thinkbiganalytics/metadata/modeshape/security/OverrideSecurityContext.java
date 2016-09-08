package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;

import org.modeshape.jcr.security.SecurityContext;

/**
 * A security context that is in effect when an administrative operation is being executed under 
 * the ModeShaepAdminPrincipal credential
 * @author Sean Felten
 */
public class OverrideSecurityContext implements SecurityContext {
    
    private final OverrideCredentials credentials;
    
    public OverrideSecurityContext(OverrideCredentials credentials) {
        super();
        this.credentials = credentials;
    }

    @Override
    public String getUserName() {
        return credentials.getUserPrincipal().getName();
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasRole(String roleName) {
        return this.credentials.getRolePrincipals().stream().anyMatch((p) -> matches(roleName, p));
    }
    
    @Override
    public void logout() {
        // Ignored
    }

    public boolean matches(String roleName, Principal principal) {
        if (principal.getName().equals(roleName)) {
            return true;
        } else if (principal instanceof Group) {
            Group group = (Group) principal;
            return Collections.list(group.members()).stream().anyMatch((p) -> matches(roleName, p));
        } else {
            return false;
        }
    }
}
