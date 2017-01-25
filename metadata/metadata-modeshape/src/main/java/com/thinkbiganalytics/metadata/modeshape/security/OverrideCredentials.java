package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
