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
import com.thinkbiganalytics.security.auth.kerberos.SpnegoValidationUserAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */
@Configuration
@EnableWebSecurity
@Order(DefaultWebSecurityConfigurer.ORDER)
@Profile("auth-krb-spnego")
public class KerberosWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Inject
    private SpnegoEntryPoint spnegoEntryPoint;

    @Inject
    private KerberosServiceAuthenticationProvider kerberosServiceAuthProvider;

    @Inject
    @Named(JaasAuthConfig.SERVICES_AUTH_PROVIDER)
    private AuthenticationProvider userPasswordAuthProvider;

    @Inject
    @Named(JaasAuthConfig.SERVICES_TOKEN_AUTH_PROVIDER)
    private AuthenticationProvider userAuthProvider;

    @Inject
    private JwtRememberMeServices rememberMeServices;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(spnegoEntryPoint)
                .and()
            .authorizeRequests()
                .antMatchers("/**").authenticated()
                .and()
            .rememberMe()
                .rememberMeServices(rememberMeServices)
                .and()
            .addFilterBefore(new RememberMeAuthenticationFilter(auth -> auth, rememberMeServices), BasicAuthenticationFilter.class)
            .addFilterAfter(spnegoFilter(), RememberMeAuthenticationFilter.class)
            .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .authenticationProvider(kerberosServiceAuthProvider)
            .authenticationProvider(userAuthProvider)
            .authenticationProvider(userPasswordAuthProvider);
    }

    @Bean
    public SpnegoValidationUserAuthenticationFilter spnegoFilter() throws Exception {
        SpnegoValidationUserAuthenticationFilter filter = new SpnegoValidationUserAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
