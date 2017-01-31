package com.thinkbiganalytics.auth.jwt;

/*-
 * #%L
 * thinkbig-security-auth
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

import com.thinkbiganalytics.auth.config.JwtProperties;

import org.joda.time.DateTimeUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtRememberMeServicesTest {

    /** The JWT Remember Me service to be tested */
    private JwtRememberMeServices service;

    /** Create service and setup environment for tests */
    @Before
    public void setUp() {
        DateTimeUtils.setCurrentMillisFixed(1461942300000L);

        final JwtProperties properties = new JwtProperties();
        properties.setAlgorithm(AlgorithmIdentifiers.HMAC_SHA256);
        properties.setKey("https://www.thinkbiganalytics.com/");
        service = new JwtRememberMeServices(properties);
    }

    /** Reset date/time after tests. */
    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    /** Verifies token deserialization. */
    @Test
    public void decodeCookie() {
        // Test with no groups
        String[] actual = service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6InVzZXIiLCJncm91cHMiOltdfQ.NKAD4m4bn1eDMGA9HSihOsLwrSUcDhBYfXIL7uJPWdo");
        String[] expected = new String[]{"user"};
        Assert.assertArrayEquals(expected, actual);

        // Test with one group
        actual = service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiYWRtaW4iXX0."
                                      + "3RX9hsmfNA1rtWMs309N5MV5_gd4FUtU_odFIIOqsoY");
        expected = new String[]{"dladmin", "admin"};
        Assert.assertArrayEquals(expected, actual);

        // Test with multiple groups
        actual = service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiZGVzaWduZXJzIiwib3BlcmF0b3JzIl19."
                                      + "fRxn00QbHAjL-R0DI1DmYfLEi3F7eMb3V2vTvgcFOy8");
        expected = new String[]{"dladmin", "designers", "operators"};
        Assert.assertArrayEquals(expected, actual);
    }

    /** Verify exception if subject is blank. */
    @Test(expected = InvalidCookieException.class)
    public void decodeCookieWithBlankSubject() {
        service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6IiIsImdyb3VwcyI6W119.TZlPnjJgAW5oP9DztgE9r10rZhMv0GAnhlbGhRiMtmA");
    }

    /** Verify exception if token is expired. */
    @Test(expected = InvalidCookieException.class)
    public void decodeCookieWithExpired() {
        service.setTokenValiditySeconds(JwtRememberMeServices.TWO_WEEKS_S);
        service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjE5NDIzMDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiYWRtaW4iXX0.AsXMAoAM1EPAw5mk4YHNXWsB9H8-lVf4JrQ6K9zHIfw");
    }

    /** Verify exception if token signature is invalid. */
    @Test(expected = InvalidCookieException.class)
    public void decodeCookieWithInvalid() {
        service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6IiIsImdyb3VwcyI6W119.L_00dw3cpWbxw32Pddj6Jq1xeFqPf8ZFdPWAUdmj39k");
    }

    /** Verify exception if subject is missing. */
    @Test(expected = InvalidCookieException.class)
    public void decodeCookieWithMissingSubject() {
        service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDB9.L_00dw3cpWbxw32Pddj6Jq1xeFqPf8ZFdPWAUdmj39k");
    }

    /** Verify exception if token is past the maximum validity. */
    @Test(expected = InvalidCookieException.class)
    public void decodeCookieWithPastMaxValidity() {
        service.setTokenValiditySeconds(604799);
        service.decodeCookie("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjI1NDcxMDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiYWRtaW4iXX0.zxjsUt53buh9hBY6nzsD1m25MnTPnDw84B3YmGgcFfk");
    }

    /** Verifies token serialization. */
    @Test
    public void encodeCookie() {
        // Test with no groups
        String actual = service.encodeCookie(new String[]{"user"});
        String expected = "eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6InVzZXIiLCJncm91cHMiOltdfQ.NKAD4m4bn1eDMGA9HSihOsLwrSUcDhBYfXIL7uJPWdo";
        Assert.assertEquals(expected, actual);

        // Test with one group
        actual = service.encodeCookie(new String[]{"dladmin", "admin"});
        expected = "eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiYWRtaW4iXX0.3RX9hsmfNA1rtWMs309N5MV5_gd4FUtU_odFIIOqsoY";
        Assert.assertEquals(expected, actual);

        // Test with multiple groups
        actual = service.encodeCookie(new String[]{"dladmin", "designers", "operators"});
        expected = "eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiZGVzaWduZXJzIiwib3BlcmF0b3JzIl19.fRxn00QbHAjL-R0DI1DmYfLEi3F7eMb3V2vTvgcFOy8";
        Assert.assertEquals(expected, actual);
    }

    /** Verify extracting tokens from authentication. */
    @Test
    public void onLoginSuccess() throws Exception {
        // Mock request, response, and authentication
        final AtomicReference<Cookie> cookie = new AtomicReference<>();
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.doAnswer(answer -> cookie.compareAndSet(null, answer.getArgumentAt(0, Cookie.class)))
                .when(response).addCookie(Mockito.any());

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContextPath()).thenReturn("");

        final Authentication auth = new UsernamePasswordAuthenticationToken("dladmin", "thinkbig", Collections.singletonList(new SimpleGrantedAuthority("admin")));

        // Test cookie set by login
        service.onLoginSuccess(request, response, auth);
        Assert.assertEquals("eyJhbGciOiJIUzI1NiIsImtpZCI6IkhNQUMifQ.eyJleHAiOjE0NjMxNTE5MDAsInN1YiI6ImRsYWRtaW4iLCJncm91cHMiOlsiYWRtaW4iXX0.3RX9hsmfNA1rtWMs309N5MV5_gd4FUtU_odFIIOqsoY",
                            cookie.get().getValue());
    }

    /** Verify building a user from tokens. */
    @Test
    public void processAutoLoginCookie() throws Exception {
        final UserDetails user = service.processAutoLoginCookie(new String[]{"dladmin", "admin"}, Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
        Assert.assertEquals("dladmin", user.getUsername());
        Assert.assertEquals(Collections.singletonList("admin").toString(), user.getAuthorities().toString());
    }
}
