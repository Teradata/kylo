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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Form Based Auth with Spring Security. Plugin a different AuthService by adding a new
 * AuthenticationProvider bean or a different AuthenticationService bean
 *
 * @see AuthenticationService
 * @see AuthServiceAuthenticationProvider
 */
@Configuration
@EnableWebSecurity
@Order(DefaultWebSecurityConfigurer.ORDER)
@Profile("!auth-krb-spnego")  // TODO find a better way than just disabling due to the presence of SPNEGO config (adjust order?)
public class DefaultWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final int ORDER = SecurityProperties.ACCESS_OVERRIDE_ORDER;
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultWebSecurityConfigurer.class);
    @Inject
    @Named(JaasAuthConfig.UI_AUTH_PROVIDER)
    private AuthenticationProvider uiAuthenticationProvider;

    @Inject
    private KyloTargetUrlLoginSuccessHandler kyloTargetUrlLoginSuccessHandler;

    @Inject
    private JwtRememberMeServices rememberMeServices;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/proxy/**", "/ui-common/**", "/assets/**","/bower_components/**","/js/vendor/**", "/images/**", "/styles/**", "/js/login/**", "/js/utils/**");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off

        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/login", "/login/**", "/login**").permitAll()
                .antMatchers("/**").authenticated()
//                .anyRequest().authenticated()
                .and()
            .formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .failureUrl("/login.html?error=true").permitAll()
               .successHandler(kyloTargetUrlLoginSuccessHandler)
                .and()
            .logout() 
                .permitAll()
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
        auth.authenticationProvider(uiAuthenticationProvider);
    }



}
