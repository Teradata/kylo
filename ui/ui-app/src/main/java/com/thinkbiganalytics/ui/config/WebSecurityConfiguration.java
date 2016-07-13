package com.thinkbiganalytics.ui.config;

import com.thinkbiganalytics.auth.AuthServiceAuthenticationProvider;
import com.thinkbiganalytics.auth.AuthenticationService;

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
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

/**
 *Form Based Auth with Spring Security.
 * Plugin a different AuthService by adding a new AuthenticationProvider bean
 * or a different AuthenticationService bean
 * @see AuthenticationService
 * @see AuthServiceAuthenticationProvider
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {


        @Autowired
        @Qualifier("authenticationProvider")
        private AuthenticationProvider authenticationProvider;

        protected static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfiguration.class);

        @Override
        public void configure(WebSecurity web) throws Exception {
                web.ignoring().antMatchers("/ui-common/**","/js/vendor/**", "/images/**", "/styles/**", "/js/login/**", "/js/utils/**");

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                http.csrf()
                    .disable()
                    .authorizeRequests()
                        .antMatchers("/login", "/login/**", "/login**").permitAll()
                        .antMatchers("/**").hasRole("USER")
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
                auth.authenticationProvider(authenticationProvider);
        }

        public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
                this.authenticationProvider = authenticationProvider;
        }


        @Configuration
        @Order(5)
        public static class RestApiAuthConfigurationAdapter extends WebSecurityConfigurerAdapter {

                @Autowired
                @Qualifier("authenticationProvider")
                private AuthenticationProvider authenticationProvider;

                @Override
                protected void configure(HttpSecurity http) throws Exception {

                        http.csrf()
                            .disable().antMatcher("/proxy/**").authorizeRequests()
                            // the ant matcher is what limits the scope of this configuration.
                            .antMatchers("/proxy/**").authenticated()
                            .and().httpBasic();//.realmName("Sourcing API");

                }


                @Override
                protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                        auth.authenticationProvider(authenticationProvider);
                }

                public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
                        this.authenticationProvider = authenticationProvider;
                }

        }
}
