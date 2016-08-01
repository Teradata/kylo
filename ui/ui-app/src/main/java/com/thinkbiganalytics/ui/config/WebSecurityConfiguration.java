package com.thinkbiganalytics.ui.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.thinkbiganalytics.auth.AuthServiceAuthenticationProvider;
import com.thinkbiganalytics.auth.AuthenticationService;

/**
 *Form Based Auth with Spring Security.
 * Plugin a different AuthService by adding a new AuthenticationProvider bean
 * or a different AuthenticationService bean
 * @see AuthenticationService
 * @see AuthServiceAuthenticationProvider
 */
@EnableWebSecurity
public class WebSecurityConfiguration {

    protected static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfiguration.class);


    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    public static class UiSecurityConfiguration extends WebSecurityConfigurerAdapter {
        
        @Autowired
        @Qualifier("uiAuthenticationProvider")
        private AuthenticationProvider uiAuthenticationProvider;

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/ui-common/**","/js/vendor/**", "/images/**", "/styles/**", "/js/login/**", "/js/utils/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            
                http
                    .csrf().disable()
                    .authorizeRequests()
                        .antMatchers("/login", "/login/**", "/login**").permitAll()
                        .antMatchers("/**").authenticated()
//                        .antMatchers("/**").hasRole("USER")
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
                        .and();

        }


        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.authenticationProvider(uiAuthenticationProvider);
        }

        public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
                this.uiAuthenticationProvider = authenticationProvider;
        }
    }


    @Configuration
    @Order(5)
    public static class RestSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier("restAuthenticationProvider")
        private AuthenticationProvider restAuthenticationProvider;

        /* (non-Javadoc)
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {

                http
                    .authenticationProvider(restAuthenticationProvider)
                    .csrf().disable()
                    .authorizeRequests()
                        // the ant matcher is what limits the scope of this configuration.
                        .antMatchers("/proxy/**").authenticated()
                        .and()
                    .httpBasic()
                        //.realmName("Sourcing API");

                    ;
        }


        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.authenticationProvider(restAuthenticationProvider);
        }

        public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
                this.restAuthenticationProvider = authenticationProvider;
        }
    }
}
