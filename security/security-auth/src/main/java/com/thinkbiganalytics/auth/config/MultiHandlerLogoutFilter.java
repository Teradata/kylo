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

import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.List;

/**
 * A logout filter that invokes all supplied LogoutHandlers and then all supplied logoutSuccessHandlers
 * during logout.  It differs from the LogoutFilter in that multiple LogoutSuccessHandlers can be specified.
 */
public class MultiHandlerLogoutFilter extends LogoutFilter {

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
}
