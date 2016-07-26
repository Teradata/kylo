package com.thinkbiganalytics.metadata.modeshape.auth;

import java.util.Map;

import javax.jcr.Credentials;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.AuthenticationProvider;
import org.springframework.security.core.Authentication;


public class SpringAuthenticationProvider implements AuthenticationProvider {

    @Override
    public ExecutionContext authenticate(Credentials credentials, 
                                         String repositoryName, 
                                         String workspaceName, 
                                         ExecutionContext repositoryContext, 
                                         Map<String, Object> sessionAttributes) {
        
        if (credentials instanceof SpringAuthenticationCredentials) {
            SpringAuthenticationCredentials cred = (SpringAuthenticationCredentials) credentials;
            Authentication auth = cred.getAuthentication();

            if (auth != null) {
                return repositoryContext.with(new SpringSecurityContext(auth));
            }
        }
        
        return null;
    }
}
