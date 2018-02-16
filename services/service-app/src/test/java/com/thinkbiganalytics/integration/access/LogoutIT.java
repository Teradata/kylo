/**
 * 
 */
package com.thinkbiganalytics.integration.access;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import static org.hamcrest.CoreMatchers.*;
import static com.thinkbiganalytics.integration.UserContext.User.ADMIN;
import static com.thinkbiganalytics.integration.UserContext.User.ANALYST;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.integration.IntegrationTestBase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests logout call pehaves correctly.  Currently only tests if the call succeeds and the 
 * appropriate redirect is returned.
 */
public class LogoutIT extends IntegrationTestBase {

    private static final Logger log = LoggerFactory.getLogger(LogoutIT.class);
    
    public static final String LOGOUT_BASE = "/v1/logout";

    
    @Test
    public void test() {
        assertLogoutDefaultRedirect();
        assertLogoutWithRedirect("/api/v1/about/me");
    }

    @Override
    protected void cleanup() {
        runAs(ADMIN);
        super.cleanup();
    }

    private void assertLogoutDefaultRedirect() {
        log.debug("LogoutIT.assertLogoutDefaultRedirect");
        runAs(ANALYST);

        Response response = given(LOGOUT_BASE).post();
        response.then().assertThat().statusCode(HTTP_MOVED_TEMP).header("Location", endsWith("/api/v1/about"));
    }
    
    private void assertLogoutWithRedirect(String redirectPath) {
        log.debug("LogoutIT.assertLogoutWithRedirect");
        runAs(ANALYST);
        
        Response response = given(LOGOUT_BASE).queryParam("redirect", redirectPath).post();
        response.then().assertThat().statusCode(HTTP_MOVED_TEMP).header("Location", endsWith(redirectPath));
    }
}
