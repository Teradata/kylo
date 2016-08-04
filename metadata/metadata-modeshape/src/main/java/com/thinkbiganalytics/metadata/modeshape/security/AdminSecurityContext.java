package com.thinkbiganalytics.metadata.modeshape.security;

import org.modeshape.jcr.security.SecurityContext;

/**
 * A security context that is in effect when an administrative operation is being executed under 
 * the ModeShaepAdminPrincipal credential
 * @author Sean Felten
 */
public class AdminSecurityContext implements SecurityContext {
    
    @Override
    public String getUserName() {
        return "dladmin";
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasRole(String roleName) {
        return true;
    }

    @Override
    public void logout() {
        // Ignored
    }
}
