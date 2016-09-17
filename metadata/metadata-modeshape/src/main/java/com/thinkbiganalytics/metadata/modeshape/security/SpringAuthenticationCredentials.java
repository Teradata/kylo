package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Credentials;

import org.springframework.security.core.Authentication;

public class SpringAuthenticationCredentials implements Credentials {

    private static final long serialVersionUID = 5674088988369122107L;
    
    private Authentication authentication;
    private Set<Principal> principals;

    public SpringAuthenticationCredentials(Authentication auth) {
        this.authentication = auth;
    }
    
    public SpringAuthenticationCredentials(Authentication auth, Principal... principals) {
        this.authentication = auth;
        this.principals = Arrays.stream(principals).collect(Collectors.toSet());
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public Set<Principal> getPrincipals() {
        return principals;
    }
}
