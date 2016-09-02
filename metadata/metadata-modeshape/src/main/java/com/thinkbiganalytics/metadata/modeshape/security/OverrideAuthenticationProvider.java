/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import java.util.Map;

import javax.jcr.Credentials;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.AuthenticationProvider;

/**
 *
 * @author Sean Felten
 */
public class OverrideAuthenticationProvider implements AuthenticationProvider {
    
    public OverrideAuthenticationProvider() {
        super();
    }

    /* (non-Javadoc)
     * @see org.modeshape.jcr.security.AuthenticationProvider#authenticate(javax.jcr.Credentials, java.lang.String, java.lang.String, org.modeshape.jcr.ExecutionContext, java.util.Map)
     */
    @Override
    public ExecutionContext authenticate(Credentials credentials, 
                                         String repositoryName, 
                                         String workspaceName, 
                                         ExecutionContext repositoryContext, 
                                         Map<String, Object> sessionAttributes) {
        if (credentials instanceof OverrideCredentials) {
            return repositoryContext.with(new OverrideSecurityContext((OverrideCredentials) credentials));
        } else {
            return null;
        }
    }

}
