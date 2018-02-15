/**
 * 
 */
package com.thinkbiganalytics.integration.access;

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
