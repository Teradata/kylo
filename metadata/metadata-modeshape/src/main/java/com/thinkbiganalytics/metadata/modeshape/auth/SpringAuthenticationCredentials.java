package com.thinkbiganalytics.metadata.modeshape.auth;

import javax.jcr.Credentials;

import org.springframework.security.core.Authentication;

public class SpringAuthenticationCredentials implements Credentials {

    private static final long serialVersionUID = 5674088988369122107L;
    
    private Authentication authentication;

    public SpringAuthenticationCredentials(Authentication auth) {
        this.authentication = auth;
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

}
