package com.thinkbiganalytics.metadata.modeshape.auth;

import org.modeshape.jcr.security.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class SpringSecurityContext implements SecurityContext {
    
    private final Authentication authentication;

    public SpringSecurityContext(Authentication auth) {
        this.authentication = auth;
    }

    @Override
    public String getUserName() {
        return this.authentication.getName();
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasRole(String roleName) {
        for (GrantedAuthority grant : this.authentication.getAuthorities()) {
            if (roleName.equals(grant.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void logout() {
        // Ignored
    }
}
