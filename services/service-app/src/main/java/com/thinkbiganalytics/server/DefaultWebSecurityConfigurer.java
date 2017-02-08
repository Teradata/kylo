package com.thinkbiganalytics.server;

import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.auth.jwt.JwtRememberMeServices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;
import javax.inject.Named;

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

/**
 * HTTP authentication with Spring Security.
 */
@Configuration
@EnableWebSecurity
@Order(DefaultWebSecurityConfigurer.ORDER)
@Profile("!auth-krb-spnego")  // TODO find a better way than just disabling due to the presence of SPNEGO config (adjust order?)
public class DefaultWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final int ORDER = SecurityProperties.ACCESS_OVERRIDE_ORDER;
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultWebSecurityConfigurer.class);
    @Inject
    @Named(JaasAuthConfig.SERVICES_AUTH_PROVIDER)
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JwtRememberMeServices rememberMeServices;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off

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
                .addFilterBefore(new RememberMeAuthenticationFilter(auth -> auth, rememberMeServices), BasicAuthenticationFilter.class)
                .httpBasic();

        // @formatter:on
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}

