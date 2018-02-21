package com.thinkbiganalytics.auth.config;

/*-
 * #%L
 * kylo-security-auth
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.apache.commons.lang3.Validate;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A LogoutHandler that directly calls the handler method of a provided ApplicationListener, passing
 * a SessionDestroyedEvent when logout is invoked.
 */
public class SessionDestroyEventLogoutHandler implements LogoutHandler {
    
    private final ApplicationListener<SessionDestroyedEvent> listener;
    
    public SessionDestroyEventLogoutHandler(ApplicationListener<SessionDestroyedEvent> provider) {
        super();
        this.listener = provider;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.logout.LogoutHandler#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            SecurityContext context = SecurityContextHolder.getContext();
            this.listener.onApplicationEvent(new ContextSessionDestroyEvent(this, context));
        }
    }
    
    private static class ContextSessionDestroyEvent extends SessionDestroyedEvent {
        
        private static final long serialVersionUID = 1L;
        
        private final SecurityContext context;
        
        public ContextSessionDestroyEvent(Object source, SecurityContext context) {
            super(source);
            Validate.notNull(context.getAuthentication(), "No Authentication found in security context");
            this.context = context;
        }

        /* (non-Javadoc)
         * @see org.springframework.security.core.session.SessionDestroyedEvent#getSecurityContexts()
         */
        @Override
        public List<SecurityContext> getSecurityContexts() {
            return Collections.singletonList(this.context);
        }

        /* (non-Javadoc)
         * @see org.springframework.security.core.session.SessionDestroyedEvent#getId()
         */
        @Override
        public String getId() {
            return context.getAuthentication().getName();
        }
    }
}
