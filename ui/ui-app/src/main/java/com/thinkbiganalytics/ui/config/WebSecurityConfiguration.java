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

import com.thinkbiganalytics.auth.AuthServiceAuthenticationProvider;
import com.thinkbiganalytics.auth.AuthenticationService;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.auth.jwt.JwtRememberMeServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

/**
 * Form Based Auth with Spring Security.
 * Plugin a different AuthService by adding a new AuthenticationProvider bean
 * or a different AuthenticationService bean
 *
 * @see AuthenticationService
 * @see AuthServiceAuthenticationProvider
 */
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    public static class UiSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier(JaasAuthConfig.UI_AUTH_PROVIDER)
        private AuthenticationProvider uiAuthenticationProvider;

        @Autowired
        private JwtRememberMeServices rememberMeServices;

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/ui-common/**", "/js/vendor/**", "/images/**", "/styles/**", "/js/login/**", "/js/utils/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                    .authorizeRequests()
                        .antMatchers("/login", "/login/**", "/login**").permitAll()
                        .antMatchers("/**").authenticated()
                        .and()
                    .formLogin()
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .failureUrl("/login.html?error=true").permitAll()
                        .and()
                    .logout()
                        .permitAll()
                        .and()
                    .rememberMe()
                        .rememberMeServices(rememberMeServices)
                        .and()
                    .addFilter(new RememberMeAuthenticationFilter(auth -> auth, rememberMeServices))
                    .httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(uiAuthenticationProvider);
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public static class ProxySecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier(JaasAuthConfig.SERVICES_AUTH_PROVIDER)
        private AuthenticationProvider restAuthenticationProvider;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authenticationProvider(restAuthenticationProvider)
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                    .authorizeRequests()
                        // the ant matcher is what limits the scope of this configuration.
                        .antMatchers("/proxy/**").authenticated()
                        .and()
                    .httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(restAuthenticationProvider);
        }
    }
}
