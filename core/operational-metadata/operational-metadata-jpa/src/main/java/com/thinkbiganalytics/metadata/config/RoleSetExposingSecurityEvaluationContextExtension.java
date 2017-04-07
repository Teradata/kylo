package com.thinkbiganalytics.metadata.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

/**
 * Created by ru186002 on 07/04/2017.
 */
public class RoleSetExposingSecurityEvaluationContextExtension extends SecurityEvaluationContextExtension {

    @Override
    public Object getRootObject() {
        Authentication authentication = getAuthentication();
        return new RoleSetExposingSecurityExpressionRoot(authentication);
    }

    private Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        return context.getAuthentication();
    }

}
