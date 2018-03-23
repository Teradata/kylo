package com.thinkbiganalytics.auth.config;

import com.thinkbiganalytics.auth.UsernameAuthenticationToken;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

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

import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A logout filter that invokes all supplied LogoutHandlers and then all supplied logoutSuccessHandlers
 * during logout.  It differs from the LogoutFilter in that multiple LogoutSuccessHandlers can be specified.
 */
public class MultiHandlerLogoutFilter extends LogoutFilter {

    /**
     * The parameter used to indicate an alternative user to be logged out
     * than the user executing the logout request.
     */
    private static final String ALT_USER_PARAM = "user";

    /**
     * Creates a new filter triggered by a logout URL to match and triggers the specified handlers to perform 
     * logout activities.  The LogoutHandlers are invoked in order followed by the LogoutSuccessHandlers 
     * if logout succeeds.
     * @param logoutUrl the URL that should be matched to indicate a logout
     * @param logoutHandlers the LogoutHandlers
     * @param logoutSuccessHandlers the LogoutSuccessHandlers
     */
    public MultiHandlerLogoutFilter(String logoutUrl, List<LogoutHandler> logoutHandlers, List<LogoutSuccessHandler> logoutSuccessHandlers) {
        super(new CompositeLogoutSuccessHandler(logoutUrl, logoutSuccessHandlers), logoutHandlers.toArray(new LogoutHandler[logoutHandlers.size()]));
        setFilterProcessesUrl(logoutUrl);
    }
    
    /**
     * Creates a new filter triggered the default logout URL to match and triggers the specified handlers to perform 
     * logout activities.  The LogoutHandlers are invoked in order followed by the LogoutSuccessHandlers 
     * if logout succeeds.
     * @param logoutUrl the URL that should be matched to indicate a logout
     * @param logoutHandlers the LogoutHandlers
     * @param logoutSuccessHandlers the LogoutSuccessHandlers
     */
    public MultiHandlerLogoutFilter(List<LogoutSuccessHandler> logoutSuccessHandlers, List<LogoutHandler> logoutHandlers) {
        this(CompositeLogoutSuccessHandler.DEFAULT_LOGOUT_URL, logoutHandlers, logoutSuccessHandlers);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.logout.LogoutFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isAlternateUser(request)) {
            if (requiresLogout(request, response)) {
                // If logout is required but the user is specified as a different user than the one currently executing this request,
                // then temporarily set the SecurityContext to be one constructed from that different user before processing the logout request.
                SecurityContext originalCxt = SecurityContextHolder.getContext();
                
                try {
                    Authentication auth = createAuthentication(request);
                    SecurityContextImpl tmpCtx = new SecurityContextImpl();
                    
                    tmpCtx.setAuthentication(auth);
                    SecurityContextHolder.setContext(tmpCtx);
                    
                    super.doFilter(req, res, chain);
                } finally {
                    SecurityContextHolder.setContext(originalCxt);
                }
            }
        } else {
            super.doFilter(req, res, chain);
        }
    }

    private Authentication createAuthentication(HttpServletRequest request) {
        String altUser = request.getParameter(ALT_USER_PARAM);
        
        if (StringUtils.isBlank(altUser)) {
            return SecurityContextHolder.getContext().getAuthentication();
        } else {
            return new UsernameAuthenticationToken(altUser);
        }
    }

    /**
     * @param request
     * @return
     */
    private boolean isAlternateUser(HttpServletRequest request) {
        return request.getParameter(ALT_USER_PARAM) != null;
    }
}
