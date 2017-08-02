package com.thinkbiganalytics.auth.rest;

/*-
 * #%L
 * REST API Authentication
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

import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.security.rest.model.User;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

public class KyloRestLoginModuleTest {

    /**
     * Verify logging in by querying a REST API.
     */
    @Test
    public void test() throws Exception {
        // Mock REST client
        final User user = new User();
        user.setEnabled(true);
        user.setGroups(ImmutableSet.of("designers", "operators"));
        user.setSystemName("dladmin");

        final JerseyRestClient client = Mockito.mock(JerseyRestClient.class);
        Mockito.when(client.get("/v1/about/me", null, User.class)).thenReturn(user);

        // Mock callback handler
        final CallbackHandler callbackHandler = callbacks -> Arrays.stream(callbacks).forEach(callback -> {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName("dladmin");
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword("thinkbig".toCharArray());
            }
        });

        // Mock login module
        final KyloRestLoginModule module = new KyloRestLoginModule() {
            @Nonnull
            @Override
            JerseyRestClient getClient(@Nonnull LoginJerseyClientConfig config) {
                return client;
            }
        };

        // Test login
        final Subject subject = new Subject();

        Map<String, Object> options = new HashMap<>();
        options.put(KyloRestLoginModule.REST_CLIENT_CONFIG, new LoginJerseyClientConfig());
        module.initialize(subject, callbackHandler, Collections.emptyMap(), options);
        Assert.assertTrue(module.login());
        Assert.assertTrue(module.commit());

        // Verify subject
        final Principal[] principals = subject.getPrincipals().toArray(new Principal[0]);
        Assert.assertEquals(3, principals.length);

        Arrays.sort(principals, Comparator.comparing(Principal::getName));
        Assert.assertEquals("designers", principals[0].getName());
        Assert.assertEquals("dladmin", principals[1].getName());
        Assert.assertEquals("operators", principals[2].getName());
    }
}
