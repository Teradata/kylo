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
import com.thinkbiganalytics.auth.config.SessionDestroyEventLogoutHandler;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;

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
public class DefaultWebSecurityConfigurer extends BaseWebSecurityConfigurer {

    protected static final Logger LOG = LoggerFactory.getLogger(DefaultWebSecurityConfigurer.class);

    /**
     * Defining these beens in an embedded configuration to ensure they are all constructed
     * before being used by the logout filter.
     */
    @Configuration
    @Profile("!auth-krb-spnego")
    static class UiHandlersConfig {
        @Inject
        @Named(JaasAuthConfig.UI_AUTH_PROVIDER)
        private AbstractJaasAuthenticationProvider authenticationProvider;

        @Bean(name="jaasLogoutHandler-ui")
        public LogoutHandler jassUiLogoutHandler() {
            // Sends a SessionDestroyEvent directly (not through a publisher) to the auth provider.
            return new SessionDestroyEventLogoutHandler(authenticationProvider);
        }
        
        @Bean(name="defaultUrlLogoutSuccessHandler-ui")
        public LogoutSuccessHandler defaultUrlLogoutSuccessHandler() {
            SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
            handler.setTargetUrlParameter("redirect");
            handler.setDefaultTargetUrl(UI_LOGOUT_REDIRECT_URL);
            return handler;
        }
    }

    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/proxy/**", "/ui-common/**", "/assets/**","/bower_components/**","/js/vendor/**", "/images/**", "/styles/**", "/js/login/**", "/js/utils/**", "/locales/**");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off

        http.removeConfigurer(LogoutConfigurer.class);

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
            .rememberMe()
                .rememberMeServices(rememberMeServices)
                .and()
            .httpBasic()
                .and()
            .addFilterBefore(jaasFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(new RememberMeAuthenticationFilter(auth -> auth, rememberMeServices), JaasApiIntegrationFilter.class)
            .addFilterAfter(logoutFilter(), BasicAuthenticationFilter.class);

        // @formatter:on
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(uiAuthenticationProvider);
    }

}
