/**
 * 
 */
package com.thinkbiganalytics.security.auth.ldap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thinkbiganalytics.auth.config.SecurityConfig;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;

/**
 *
 * @author Sean Felten
 */
@SpringApplicationConfiguration(classes = { 
                                            SecurityConfig.class, 
                                            JaasAuthConfig.class, 
                                            LdapAuthConfig.class, 
                                            LdapLoginModuleTestConfig.class 
                                            })
@TestPropertySource("classpath:ldap-test.properties")
@ConfigurationProperties(locations="classpath:ldap-test.properties")
public class LdapLoginModuleTest extends AbstractTestNGSpringContextTests {
    
    @Inject
    private LdapAuthenticator authenticator;
    
    @Inject
    private LdapAuthoritiesPopulator authPopulator;

    private LdapLoginModule loginModule;
    
    private Subject subject;
    
//    @BeforeMethod
    public void setup() {
        Map<String, Object> options = new HashMap<>();
        options.put(LdapLoginModule.AUTHENTICATOR, this.authenticator);
        options.put(LdapLoginModule.AUTHORITIES_POPULATOR, this.authPopulator);
        
        this.subject = new Subject();
        this.loginModule = new LdapLoginModule();
        this.loginModule.initialize(new Subject(), 
                                    createHandler("admin", "admin"), 
                                    new HashMap<>(), 
                                    options);
    }
    
//    @Test
    public void testLogin() throws Exception {
//        boolean success = this.loginModule.logout();
        this.loginModule.logout();
        
//        assertThat(success).isTrue();
    }
    
    
    private CallbackHandler createHandler(String user, String password) {
        return new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(user);
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                    }
                }
            }
        };
    }
}
