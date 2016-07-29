/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.auth;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import com.thinkbiganalytics.auth.jaas.AbstractLoginModule;

/**
 *
 * @author Sean Felten
 */
public class ModeShapeLoginModule extends AbstractLoginModule {
    
    private ModeShapePrincipal principal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
    }

    @Override
    protected boolean doLogin() throws Exception {
        // No login behavior required; assume login success
        return true;
    }

    @Override
    protected boolean doCommit() throws Exception {
        // For now assume everyone has read/write.
        this.principal = new ModeShapeReadWritePrincipal();
        getSubject().getPrincipals().add(this.principal);
        return true;
    }

    @Override
    protected boolean doAbort() throws Exception {
        return logout();
    }

    @Override
    protected boolean doLogout() throws Exception {
        getSubject().getPrincipals().remove(this.principal);
        return true;
    }
}
