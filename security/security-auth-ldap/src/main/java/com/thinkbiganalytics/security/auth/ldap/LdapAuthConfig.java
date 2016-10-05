/**
 * 
 */
package com.thinkbiganalytics.security.auth.ldap;

import java.net.URI;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${security.auth.ldap.login.ui:required}")
    private String uiLoginFlag;
    
    @Value("${security.auth.ldap.login.services:required}")
    private String servicesLoginFlag;

    @Bean(name = "servicesLdapLoginConfiguration")
    public LoginConfiguration servicesLdapLoginConfiguration(LdapAuthenticator authenticator,
                                                             LdapAuthoritiesPopulator authoritiesPopulator,
                                                             LoginConfigurationBuilder builder) {
        return builder
                .loginModule(JaasAuthConfig.JAAS_SERVICES)
                    .moduleClass(LdapLoginModule.class)
                    .controlFlag(this.servicesLoginFlag)
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
                    .controlFlag(this.uiLoginFlag)
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
    @ConfigurationProperties("security.auth.ldap.user")
    public LdapAuthoritiesPopulatorFactory ldapAuthoritiesPopulator(LdapContextSource context) {
        return new LdapAuthoritiesPopulatorFactory(context);
    }
    
    
    public static class LdapContextSourceFactory extends AbstractFactoryBean<LdapContextSource> {
        
        private URI uri;
        private String authDn;
        private char[] password = "".toCharArray();
        
        public void setUri(String uri) {
            this.uri = URI.create(uri);
        }
        
        public void setAuthDn(String userDn) {
            this.authDn = userDn;
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
            if (StringUtils.isNotEmpty(this.authDn)) cxt.setUserDn(this.authDn);
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
        private String groupsBase;
        private String groupNameAttr;
        private boolean enableGroups = false;
        
        public LdapAuthoritiesPopulatorFactory(LdapContextSource contextSource) {
            super();
            this.contextSource = contextSource;
        }
        
        public void setGroupsBase(String groupsOu) {
            this.groupsBase = groupsOu;
        }
        
        public void setGroupNameAttr(String groupRoleAttribute) {
            this.groupNameAttr = groupRoleAttribute;
        }
        
        public void setEnableGroups(boolean enabled) {
            this.enableGroups = enabled;
        }
        
        @Override
        public Class<?> getObjectType() {
            return LdapAuthoritiesPopulator.class;
        }

        @Override
        protected LdapAuthoritiesPopulator createInstance() throws Exception {
            if (this.enableGroups) {
                DefaultLdapAuthoritiesPopulator authPopulator = new DefaultLdapAuthoritiesPopulator(this.contextSource, this.groupsBase);
                authPopulator.setGroupRoleAttribute(this.groupNameAttr);
                authPopulator.setRolePrefix("");
                authPopulator.setConvertToUpperCase(false);
                return authPopulator;
            } else {
                return new NullLdapAuthoritiesPopulator();
            }
        }
    }
}
