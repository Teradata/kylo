package com.thinkbiganalytics.ui.config.kerberos;

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

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.thinkbiganalytics.ui.config.DefaultWebSecurityConfigurerAdapter;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableWebSecurity
@Profile("auth-krb-spnego")
@Order(DefaultWebSecurityConfigurerAdapter.ORDER + 1)
public class KerberosWebSecurityConfiguration extends DefaultWebSecurityConfigurerAdapter {
    
    @Inject 
    private SpnegoEntryPoint spnegoEntryPoint;
    
    @Inject
    private KerberosServiceAuthenticationProvider kerberosServiceAuthProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        
        http
            .exceptionHandling()
                .authenticationEntryPoint(spnegoEntryPoint)
                .and()
            .addFilterBefore(spnegoAuthenticationProcessingFilter(), BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        
        auth.authenticationProvider(kerberosServiceAuthProvider);
    }
    
    @Bean
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter() throws Exception {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }

    @Bean(name="krbAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
