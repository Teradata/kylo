package com.thinkbiganalytics.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.thinkbiganalytics.auth.jaas.JaasAuthConfig;

/**
 */
@EnableWebSecurity
public class RestSecurityConfiguration extends WebSecurityConfigurerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(RestSecurityConfiguration.class);


    @Autowired
    @Qualifier(JaasAuthConfig.SERVICES_AUTH_PROVIDER)
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
            http
                .authenticationProvider(this.authenticationProvider)
                .csrf()
                .disable()
                .authorizeRequests()
                    .antMatchers("/**").authenticated()
                    .and()
                .httpBasic();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider);
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
            this.authenticationProvider = authenticationProvider;
    }
}
