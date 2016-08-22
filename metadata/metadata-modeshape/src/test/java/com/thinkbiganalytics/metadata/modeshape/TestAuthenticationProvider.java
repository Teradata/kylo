/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.util.Map;

import javax.jcr.Credentials;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.security.AuthenticationProvider;

/**
 *
 * @author Sean Felten
 */
public class TestAuthenticationProvider implements AuthenticationProvider {

    /* (non-Javadoc)
     * @see org.modeshape.jcr.security.AuthenticationProvider#authenticate(javax.jcr.Credentials, java.lang.String, java.lang.String, org.modeshape.jcr.ExecutionContext, java.util.Map)
     */
    @Override
    public ExecutionContext authenticate(Credentials credentials, 
                                         String repositoryName, 
                                         String workspaceName, 
                                         ExecutionContext repositoryContext, 
                                         Map<String, Object> sessionAttributes) {
        if (credentials instanceof TestCredentials) {
            return repositoryContext.with(new TestSecurityContext());
        } else {
            return null;
        }
    }

}
