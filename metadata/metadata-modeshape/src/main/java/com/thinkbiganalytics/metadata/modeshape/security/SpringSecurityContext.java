package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.modeshape.jcr.security.SecurityContext;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;

/**
 * A security context that is in effect when an operation is being executed with 
 * the credential authenticated via Spring security.
 * @author Sean Felten
 */
public class SpringSecurityContext implements SecurityContext {
    
    private final Authentication authentication;
    private final Set<Principal> principals;

    public SpringSecurityContext(Authentication auth) {
        this(auth, Collections.emptySet());
    }
    
    public SpringSecurityContext(Authentication auth, Collection<Principal> additionalPrincipals) {
        this.authentication = auth;
        this.principals = Collections.unmodifiableSet(new HashSet<>(additionalPrincipals));
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
        boolean matched = this.authentication.getAuthorities().stream().anyMatch(grant -> {
            if (grant instanceof JaasGrantedAuthority) {
                JaasGrantedAuthority jaasGrant = (JaasGrantedAuthority) grant;

                return matches(roleName, jaasGrant.getPrincipal());
            } else {
                if (roleName.equals(grant.getAuthority())) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        
        if (matched) {
            return true;
        } else {
            return this.principals.stream().anyMatch(principal -> matches(roleName, principal));
        }
    }

    @Override
    public void logout() {
        // Ignored
    }

    private boolean matches(String roleName, Principal principal) {
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
