/**
 * 
 */
package com.thinkbiganalytics.auth.jaas;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;

import com.thinkbiganalytics.auth.RolePrincipalAuthorityGranter;
import com.thinkbiganalytics.auth.UserRoleAuthorityGranter;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JaasAuthConfig {

    @Bean(name = "uiAuthenticationProvider")
    public AuthenticationProvider uiAuthenticationProvider(@Named("jaasConfiguration") javax.security.auth.login.Configuration config,
                                                           List<AuthorityGranter> authorityGranters) {
        DefaultJaasAuthenticationProvider provider = new DefaultJaasAuthenticationProvider();
        provider.setConfiguration(config);
        provider.setAuthorityGranters(authorityGranters.toArray(new AuthorityGranter[authorityGranters.size()]));
        provider.setLoginContextName("UI");
        return provider;
    }
    
    @Bean(name = "restAuthenticationProvider")
    public AuthenticationProvider restAuthenticationProvider(@Named("jaasConfiguration") javax.security.auth.login.Configuration config,
                                                             List<AuthorityGranter> authorityGranters) {
        DefaultJaasAuthenticationProvider provider = new DefaultJaasAuthenticationProvider();
        provider.setConfiguration(config);
        provider.setAuthorityGranters(authorityGranters.toArray(new AuthorityGranter[authorityGranters.size()]));
        provider.setLoginContextName("REST");
        return provider;
    }

    @Bean(name="jaasConfiguration")
    public javax.security.auth.login.Configuration jaasConfiguration(List<Map<String, AppConfigurationEntry[]>> loginModuleEntries) {
        Map<String, AppConfigurationEntry[]> merged = loginModuleEntries.stream()
            .map(m -> m.entrySet())
            .flatMap(s -> s.stream())
            .collect(Collectors.toMap(e -> e.getKey(), 
                                      e -> e.getValue(),
                                      ArrayUtils::addAll));

        return new InMemoryConfiguration(merged);
    }
    
    @Bean
    public AuthorityGranter rolePrincipalAuthorityGranter() {
        return new RolePrincipalAuthorityGranter();
    }
    
    @Bean
    public AuthorityGranter userRoleAuthorityGranter() {
        return new UserRoleAuthorityGranter();
    }
}
