package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import org.modeshape.jcr.security.SecurityContext;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * A security context that is in effect when an operation is being executed with 
 * the credential authenticated via Spring security.
 * @author Sean Felten
 */
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
            // If this is a JaasGrantedAuthority then we will test against its principal.
            if (grant instanceof JaasGrantedAuthority) {
                JaasGrantedAuthority jaasGrant = (JaasGrantedAuthority) grant;

                if (roleName.equals(jaasGrant.getPrincipal().getName())) {
                    return true;
                }

                // If this is a group principal then we will test against each of its embedded member principals.
                if (jaasGrant.getPrincipal() instanceof Group) {
                    Group group = (Group) jaasGrant.getPrincipal();
                    Enumeration<? extends Principal> members = group.members();
                    
                    while (members.hasMoreElements()) {
                        Principal principal = (Principal) members.nextElement();
                        
                        if (roleName.equals(principal.getName())) {
                            return true;
                        }
                    }
                }
            } else {
                if (roleName.equals(grant.getAuthority())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void logout() {
        // Ignored
    }
}
