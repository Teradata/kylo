package com.thinkbiganalytics.metadata.modeshape.auth;

import org.modeshape.jcr.security.SecurityContext;

public class AdminSecurityContext implements SecurityContext {
    
    @Override
    public String getUserName() {
        return "dbadmin";
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
