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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A composite of multiple LogouSuccessHandlers.  A SimpleUrlLogoutSuccessHandler type of handler will always be 
 * added if none are present in the list of handlers provided this composite.
 */
public class CompositeLogoutSuccessHandler implements LogoutSuccessHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CompositeLogoutSuccessHandler.class);
    
    public static final String DEFAULT_LOGOUT_URL = "/logout";

    private final List<LogoutSuccessHandler> componentHandlers = new ArrayList<>();
    
    /**
     * Constructs an instance containing just the default handler ({@link SimpleUrlLogoutSuccessHandler})
     * using the default logout URL.
     */
    public CompositeLogoutSuccessHandler() {
        this(DEFAULT_LOGOUT_URL, Collections.emptyList());
    }

    /**
     * Constructs an instance containing the given component handlers.  Ensures that there will
     * be a AbstractAuthenticationTargetUrlRequestHandler among the handlers; creating one if necessary
     * using the default logout URL.
     */
    public CompositeLogoutSuccessHandler(List<LogoutSuccessHandler> handlers) {
        this(DEFAULT_LOGOUT_URL, handlers);
    }

    /**
     * Constructs an instance containing the given component handlers.  Ensures that there will
     * be a AbstractAuthenticationTargetUrlRequestHandler among the handlers; creating one if necessary
     * using the supplied logoutUrl.
     */
    public CompositeLogoutSuccessHandler(String logoutUrl, List<LogoutSuccessHandler> handlers) {
        Validate.notEmpty(logoutUrl, "Logout URL must not be empty");
        
        LogoutSuccessHandler defHandler = handlers.stream()
                        .filter(h -> h instanceof AbstractAuthenticationTargetUrlRequestHandler)
                        .findFirst()
                        .orElse(createUrlLogoutSuccessHandler(logoutUrl));
                    
        this.componentHandlers.add(defHandler);
        handlers.stream()
            .filter(h -> ! (h instanceof AbstractAuthenticationTargetUrlRequestHandler))
            .forEach(this.componentHandlers::add);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.logout.LogoutSuccessHandler#onLogoutSuccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        IOException ioe = null; 
        ServletException se = null;
        RuntimeException re = null;
        
        // Ensure all componentHandlers get called even though there is an exception thrown;
        for (LogoutSuccessHandler handler : this.componentHandlers) {
            try {
                handler.onLogoutSuccess(request, response, authentication);
            } catch (IOException e) {
                log.error("IOException thrown by a LogoutSuccessHandler after logout", e);
                if (ioe != null) ioe = e;
            } catch (ServletException e) {
                log.error("ServletException thrown by a LogoutSuccessHandler after logout", e);
                if (se != null) se = e;
            } catch (RuntimeException e) {
                log.error("RuntimeException thrown by a LogoutSuccessHandler after logout", e);
                if (re != null) re = e;
            }
        }
        
        // Throw one of the exceptions if any occurred.
        if (ioe != null) throw ioe;
        if (se != null) throw se;
        if (re != null) throw re;
    }

    private SimpleUrlLogoutSuccessHandler createUrlLogoutSuccessHandler(String logoutUrl) {
        return new SimpleUrlLogoutSuccessHandler();
    }

}
