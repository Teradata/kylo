/**
 *
 */
package com.thinkbiganalytics.security.auth.ldap;

/*-
 * #%L
 * thinkbig-security-auth-ldap
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

import com.thinkbiganalytics.auth.config.SecurityConfig;
import com.thinkbiganalytics.auth.jaas.config.JaasAuthConfig;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

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
import javax.security.auth.login.LoginException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@SpringApplicationConfiguration(classes = {
    SecurityConfig.class,
    JaasAuthConfig.class,
    LdapAuthConfig.class,
    LdapLoginModuleTestConfig.class
})
@TestPropertySource("classpath:ldap-test.properties")
@ActiveProfiles("auth-ldap")
public class LdapLoginModuleTest extends AbstractTestNGSpringContextTests {

    @Inject
    private LdapAuthenticator authenticator;

    @Inject
    private LdapAuthoritiesPopulator authPopulator;


//    @Test
    public void testLoginAdmin() throws Exception {
        Subject subject = login("dladmin", "thinkbig");

        assertThat(subject.getPrincipals()).hasSize(2).contains(new UsernamePrincipal("dladmin"), new GroupPrincipal("admin"));
    }

//    @Test
    public void testLoginTest() throws Exception {
        Subject subject = login("test", "user");

        assertThat(subject.getPrincipals()).hasSize(3).contains(new UsernamePrincipal("test"),
                                                                new GroupPrincipal("admin"),
                                                                new GroupPrincipal("developer"));
    }

//    @Test(expectedExceptions = LoginException.class)
    public void testLoginBogus() throws Exception {
        login("bogus", "user");
    }

    private Subject login(String user, String password) throws LoginException {
        Map<String, Object> options = new HashMap<>();
        options.put(LdapLoginModule.AUTHENTICATOR, this.authenticator);
        options.put(LdapLoginModule.AUTHORITIES_POPULATOR, this.authPopulator);

        Subject subject = new Subject();
        LdapLoginModule module = new LdapLoginModule();

        module.initialize(subject,
                          createHandler(user, password),
                          new HashMap<>(),
                          options);

        try {
            boolean success = module.login();

            if (success) {
                module.commit();
            }

            return subject;
        } catch (LoginException e) {
            module.abort();
            throw e;
        }
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
