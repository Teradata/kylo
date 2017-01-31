package com.thinkbiganalytics.security.auth.kerberos;

/*-
 * #%L
 * thinkbig-security-auth-kerberos
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

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableWebSecurity
@Profile("auth-krb-spnego")
public class KerberosSpnegoConfiguration {

    @Value("${security.auth.krb.service-principal}")
    private String servicePrincipal;

    @Value("${security.auth.krb.keytab}")
    private String keytabLocation;

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        return new SpnegoEntryPoint();
//        return new SpnegoEntryPoint("/login");
    }

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws IOException {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(new DummyUserDetailsManager());
        return provider;
    }

    @Bean
    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
        ticketValidator.setServicePrincipal(servicePrincipal);
        ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
        ticketValidator.setDebug(true);
        return ticketValidator;
    }

    /**
     * Since the SPNEGO filer requires a UserDetailsManager we given it this one since we 
     * do not use UserDetailsManager to load user info in Kylo.
     */
    private class DummyUserDetailsManager extends InMemoryUserDetailsManager {

        public DummyUserDetailsManager() {
            super(Collections.emptyList());
        }
        
        /* (non-Javadoc)
         * @see org.springframework.security.provisioning.InMemoryUserDetailsManager#loadUserByUsername(java.lang.String)
         */
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // Return a new dummy user each time from the already authenticated username.
            return new User(username, "", Collections.singleton(new SimpleGrantedAuthority("admin")));
        }
    }
}
