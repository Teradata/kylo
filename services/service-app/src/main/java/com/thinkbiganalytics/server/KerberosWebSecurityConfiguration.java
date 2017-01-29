/**
 * 
 */
package com.thinkbiganalytics.server;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableWebSecurity
@Profile("auth-krb")
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER + 1)
public class KerberosWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.auth.krb.service-principal}")
    private String servicePrincipal;

    @Value("${security.auth.krb.keytab}")
    private String keytabLocation;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(spnegoEntryPoint())
                .and()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .addFilterBefore(spnegoAuthenticationProcessingFilter(authenticationManagerBean()),
                             BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .authenticationProvider(kerberosAuthenticationProvider())
            .authenticationProvider(kerberosServiceAuthenticationProvider());
    }
    
    @Bean(name="krbAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        AuthenticationManager mgr = super.authenticationManagerBean();
        return mgr;
    }

    @Bean
    public KerberosAuthenticationProvider kerberosAuthenticationProvider() throws IOException {
        KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
        SunJaasKerberosClient client = new SunJaasKerberosClient();
        client.setDebug(true);
        provider.setKerberosClient(client);
        
        Properties users = new Properties();
        users.load(new StringReader("dladmin@EXAMPLE.ORG=thinkbig,admin"));
        
        provider.setUserDetailsService(new InMemoryUserDetailsManager(users));
        return provider;
    }

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        return new SpnegoEntryPoint();
    }

    @Bean
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws IOException {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        
        Properties users = new Properties();
        users.load(new StringReader("dladmin@EXAMPLE.ORG=thinkbig,admin"));
        
        provider.setUserDetailsService(new InMemoryUserDetailsManager(users));
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

}
