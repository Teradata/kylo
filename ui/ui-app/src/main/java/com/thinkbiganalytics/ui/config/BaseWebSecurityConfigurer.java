package com.thinkbiganalytics.ui.config;

/*-
 * #%L
 * thinkbig-ui-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.thinkbiganalytics.auth.config.MultiHandlerLogoutFilter;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.auth.jwt.JwtRememberMeServices;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Abstract base HTTP authentication configurer for Spring Security.
 */
public abstract class BaseWebSecurityConfigurer extends WebSecurityConfigurerAdapter {
    
    public static final int ORDER = SecurityProperties.ACCESS_OVERRIDE_ORDER;
    public static final String UI_LOGOUT_URL = "/logout";
    public static final String UI_LOGOUT_REDIRECT_URL = "/login.html?logout";
//    public static final String UI_LOGOUT_REDIRECT_URL = "/index.html";
    
    @Inject
    @Named(JaasAuthConfig.UI_AUTH_PROVIDER)
    protected AuthenticationProvider uiAuthenticationProvider;

    @Inject
    protected KyloTargetUrlLoginSuccessHandler kyloTargetUrlLoginSuccessHandler;

    @Inject
    protected JwtRememberMeServices rememberMeServices;
    
    @Inject
    protected Optional<List<LogoutSuccessHandler>> logoutSuccessHandlers;
    
    @Inject
    protected Optional<List<LogoutHandler>> logoutHandlers;
    
    
    @Bean
    public LogoutFilter logoutFilter() {
        return new MultiHandlerLogoutFilter(UI_LOGOUT_URL, 
                                            logoutHandlers.orElse(Collections.emptyList()), 
                                            logoutSuccessHandlers.orElse(Collections.emptyList()));
    }
    
    @Bean
    public JaasApiIntegrationFilter jaasFilter() {
        JaasApiIntegrationFilter filter = new JaasApiIntegrationFilter();
        filter.setCreateEmptySubject(true);
        return filter;
    }
}
