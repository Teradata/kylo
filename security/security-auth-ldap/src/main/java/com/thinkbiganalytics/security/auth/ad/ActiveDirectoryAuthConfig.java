/**
 * 
 */
package com.thinkbiganalytics.security.auth.ad;

import java.net.URI;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

/**
 * Active Directory login configuration.
 * 
 * @author Sean Felten
 */
@Configuration
@Profile("auth-ad")
public class ActiveDirectoryAuthConfig {
    
    @Bean(name = "servicesActiveDirectoryLoginConfiguration")
    public LoginConfiguration servicesAdLoginConfiguration(ActiveDirectoryLdapAuthenticationProvider authProvider,
                                                           UserDetailsContextMapper userMapper,
                                                           LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(ActiveDirectoryLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED)
                    .option(ActiveDirectoryLoginModule.AUTH_PROVIDER, authProvider)
                    .option(ActiveDirectoryLoginModule.USER_MAPPER, userMapper)
                    .add()
                .build();
    }
    
    @Bean(name = "uiActiveDirectoryLoginConfiguration")
    public LoginConfiguration uiAdLoginConfiguration(ActiveDirectoryLdapAuthenticationProvider authProvider,
                                                     UserDetailsContextMapper userMapper,
                                                     LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(ActiveDirectoryLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.REQUIRED)
                    .option(ActiveDirectoryLoginModule.AUTH_PROVIDER, authProvider)
                    .option(ActiveDirectoryLoginModule.USER_MAPPER, userMapper)
                    .add()
                .build();
    }


    @Bean
    @ConfigurationProperties("security.auth.ad.user")
    protected UserDetailsMapperFactory userDetailsContextMapper()  {
        return new UserDetailsMapperFactory();
    }
    
    @Bean
    @ConfigurationProperties("security.auth.ad.server")
    protected ActiveDirectoryProviderFactory activeDirectoryAuthenticationProvider() {
        return new ActiveDirectoryProviderFactory();
    }
    
    
    public static class ActiveDirectoryProviderFactory extends AbstractFactoryBean<ActiveDirectoryLdapAuthenticationProvider> {
        
        private URI uri;
        private String domain;

        public void setUri(String uri) {
            this.uri = URI.create(uri);
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
        
        @Override
        public Class<?> getObjectType() {
            return ActiveDirectoryLdapAuthenticationProvider.class;
        }

        @Override
        protected ActiveDirectoryLdapAuthenticationProvider createInstance() throws Exception {
            ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(this.domain, this.uri.toASCIIString());
            provider.setConvertSubErrorCodesToExceptions(true);

            return provider;
        }
    }
    
    public static class UserDetailsMapperFactory extends AbstractFactoryBean<UserDetailsContextMapper> {
        
        private String passwordAttribute = null;
        private String[] groupAttributes = null;
        
        public void setPasswordAttribute(String passwordAttribute) {
            this.passwordAttribute = passwordAttribute;
        }

        public void setGroupAttribures(String groupAttribures) {
            this.groupAttributes = groupAttribures.split("\\|");
        }

        @Override
        public Class<?> getObjectType() {
            return UserDetailsContextMapper.class;
        }
        
        @Override
        protected UserDetailsContextMapper createInstance() throws Exception {
            LdapUserDetailsMapper mapper = new LdapUserDetailsMapper();
            mapper.setConvertToUpperCase(false);
            mapper.setRolePrefix("");
            if (StringUtils.isNotEmpty(this.passwordAttribute)) mapper.setPasswordAttributeName(passwordAttribute);
            if (ArrayUtils.isNotEmpty(this.groupAttributes)) mapper.setRoleAttributes(groupAttributes);
            
            return mapper;
        }
    }
}
