/**
 * 
 */
package com.thinkbiganalytics.security.auth.ldap;

import java.net.URI;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.authentication.NullLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.auth.jaas.LoginConfigurationBuilder;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

/**
 * LDAP login configuration.
 * 
 * @author Sean Felten
 */
@Configuration
@Profile("auth-ldap")
public class LdapAuthConfig {
    
    @Bean(name = "servicesLdapLoginConfiguration")
    public LoginConfiguration servicesLdapLoginConfiguration(LdapAuthenticator authenticator,
                                                             LdapAuthoritiesPopulator authoritiesPopulator,
                                                             LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(LdapLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(LdapLoginModule.AUTHENTICATOR, authenticator)
                    .option(LdapLoginModule.AUTHORITIES_POPULATOR, authoritiesPopulator)
                    .add()
                .build();
    }
    
    @Bean(name = "uiLdapLoginConfiguration")
    public LoginConfiguration uiLdapLoginConfiguration(LdapAuthenticator authenticator,
                                                       LdapAuthoritiesPopulator authoritiesPopulator,
                                                       LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_UI)
                    .moduleClass(LdapLoginModule.class)
                    .controlFlag(AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL)
                    .option(LdapLoginModule.AUTHENTICATOR, authenticator)
                    .option(LdapLoginModule.AUTHORITIES_POPULATOR, authoritiesPopulator)
                    .add()
                .build();
    }


    @Bean
    @ConfigurationProperties("security.auth.ldap.server")
    public LdapContextSourceFactory ldapContextSource() {
        return new LdapContextSourceFactory();
    }
    
    @Bean
    @ConfigurationProperties("security.auth.ldap.authenticator")
    public LdapAuthenticatorFactory ldapAuthenticator(LdapContextSource context) {
        return new LdapAuthenticatorFactory(context);
    }
    
    @Bean
    @ConfigurationProperties("security.auth.ldap.groups")
    public LdapAuthoritiesPopulatorFactory ldapAuthoritiesPopulator(LdapContextSource context) {
        return new LdapAuthoritiesPopulatorFactory(context);
    }
    
    
    public static class LdapContextSourceFactory extends AbstractFactoryBean<LdapContextSource> {
        
        private URI uri;
        private String userDn;
        private char[] password = "".toCharArray();
        
        public void setUri(String uri) {
            this.uri = URI.create(uri);
        }
        
        public void setUserDn(String userDn) {
            this.userDn = userDn;
        }

        public void setPassword(String password) {
            this.password = password.toCharArray();
        }

        @Override
        public Class<?> getObjectType() {
            return LdapContextSource.class;
        }

        @Override
        protected LdapContextSource createInstance() throws Exception {
            DefaultSpringSecurityContextSource cxt = new DefaultSpringSecurityContextSource(this.uri.toASCIIString());
            if (StringUtils.isNotEmpty(this.userDn)) cxt.setUserDn(this.userDn);
            if (ArrayUtils.isNotEmpty(this.password)) cxt.setPassword(new String(this.password));
            cxt.setCacheEnvironmentProperties(false);
            cxt.afterPropertiesSet();
            return cxt;
        }
    }
    
    public static class LdapAuthenticatorFactory extends AbstractFactoryBean<LdapAuthenticator> {
        
        private LdapContextSource contextSource;
        private String[] userDnPatterns;
        
        public LdapAuthenticatorFactory(LdapContextSource contextSource) {
            super();
            this.contextSource = contextSource;
        }
        
        public void setUserDnPatterns(String userDnPatterns) {
            this.userDnPatterns = userDnPatterns.split("\\|");
        }

        @Override
        public Class<?> getObjectType() {
            return LdapAuthenticator.class;
        }

        @Override
        protected LdapAuthenticator createInstance() throws Exception {
            BindAuthenticator auth = new BindAuthenticator(this.contextSource);
            auth.setUserDnPatterns(userDnPatterns);
            return auth;
        }
    }

    public static class LdapAuthoritiesPopulatorFactory extends AbstractFactoryBean<LdapAuthoritiesPopulator> {

        private LdapContextSource contextSource;
        private String groupsOu;
        private String groupRoleAttribute;
        private boolean enabled = false;
        
        public LdapAuthoritiesPopulatorFactory(LdapContextSource contextSource) {
            super();
            this.contextSource = contextSource;
        }
        
        public void setGroupsOu(String groupsOu) {
            this.groupsOu = groupsOu;
        }
        
        public void setGroupRoleAttribute(String groupRoleAttribute) {
            this.groupRoleAttribute = groupRoleAttribute;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        @Override
        public Class<?> getObjectType() {
            return LdapAuthoritiesPopulator.class;
        }

        @Override
        protected LdapAuthoritiesPopulator createInstance() throws Exception {
            if (this.enabled) {
                DefaultLdapAuthoritiesPopulator authPopulator = new DefaultLdapAuthoritiesPopulator(this.contextSource, this.groupsOu);
                authPopulator.setGroupRoleAttribute(this.groupRoleAttribute);
                authPopulator.setRolePrefix("");
                authPopulator.setConvertToUpperCase(false);
                return authPopulator;
            } else {
                return new NullLdapAuthoritiesPopulator();
            }
        }
    }
}
