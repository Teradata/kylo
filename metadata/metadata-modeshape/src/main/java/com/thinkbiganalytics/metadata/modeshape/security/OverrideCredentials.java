package com.thinkbiganalytics.metadata.modeshape.security;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Credentials;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 * Credentials used to override those derived from the current security context.
 * 
 * @author Sean Felten
 */
public class OverrideCredentials implements Credentials {
    
    private static final long serialVersionUID = 1L;

    private final Principal userPrincipal;
    private final Set<Principal> rolePrincipals;
    
    public static OverrideCredentials create(Principal... principals) {
        return create(Arrays.asList(principals));
    }
    
    public static OverrideCredentials create(Iterable<Principal> principals) {
        Principal user = null;
        Set<Principal> roleSet = new HashSet<>();
        
        for (Principal principal : principals) {
            if (user == null && isUser(principal)) {
                user = principal;
            } else {
                roleSet.add(principal);
            }
        }
        
        if (user == null) {
            user = MetadataAccess.ANONYMOUS;
        }
        
        if (MetadataAccess.SERVICE.equals(user)) {
            roleSet.add(MetadataAccess.ADMIN);
        }
        
        return new OverrideCredentials(user, roleSet);
    }
    
    private static boolean isUser(Principal principal) {
        return principal instanceof UsernamePrincipal;  // Others?
    }

    public OverrideCredentials(Principal userPrincipal, Set<Principal> rolePrincipals) {
        super();
        this.userPrincipal = userPrincipal;
        this.rolePrincipals = Collections.unmodifiableSet(new HashSet<>(rolePrincipals));
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public Set<Principal> getRolePrincipals() {
        return rolePrincipals;
    }
    
}
