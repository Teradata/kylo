package com.thinkbiganalytics.server;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.auth.jwt.JwtRememberMeServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

/**
 * HTTP authentication with Spring Security.
 */
@Configuration
@EnableWebSecurity
@Order(DefaultWebSecurityConfigurerAdapter.ORDER)
public class DefaultWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(DefaultWebSecurityConfigurerAdapter.class);
    
    public static final int ORDER = SecurityProperties.ACCESS_OVERRIDE_ORDER;

    @Inject
    @Named(JaasAuthConfig.SERVICES_AUTH_PROVIDER)
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JwtRememberMeServices rememberMeServices;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(this.authenticationProvider)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers("/**").authenticated()
                    .and()
                .rememberMe()
                    .rememberMeServices(rememberMeServices)
                    .and()
                .addFilter(new RememberMeAuthenticationFilter(auth -> auth, rememberMeServices))
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}

