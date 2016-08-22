package com.thinkbiganalytics.metadata.modeshape;

import org.modeshape.jcr.security.SecurityContext;

/**
 * A security context that is in effect when an administrative operation is being executed under 
 * the ModeShaepAdminPrincipal credential
 * @author Sean Felten
 */
public class TestSecurityContext implements SecurityContext {
    
    @Override
    public String getUserName() {
        return "test";
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasRole(String roleName) {
        return roleName.equalsIgnoreCase("user") || roleName.equalsIgnoreCase("readwrite");
    }

    @Override
    public void logout() {
        // Ignored
    }
}
