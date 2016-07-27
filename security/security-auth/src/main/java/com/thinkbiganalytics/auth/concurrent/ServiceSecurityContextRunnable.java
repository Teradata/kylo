/**
 * 
 */
package com.thinkbiganalytics.auth.concurrent;

import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thinkbiganalytics.auth.ServiceAuthenticatoinToken;

/**
 *
 * @author Sean Felten
 */
public class ServiceSecurityContextRunnable implements Runnable {

    private final DelegatingSecurityContextRunnable delegate;
    
    /**
     * 
     */
    public ServiceSecurityContextRunnable(Runnable runnable) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new ServiceAuthenticatoinToken());
        
        this.delegate = new DelegatingSecurityContextRunnable(runnable, context);
    }

    @Override
    public void run() {
        this.delegate.run();
    }
}
