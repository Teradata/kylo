/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.http;

/*-
 * #%L
 * kylo-security-auth
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class JaasHttpCallbackHandlerFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private JaasHttpCallbackHandlerFilter filter = new JaasHttpCallbackHandlerFilter();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testExistingHeader() throws IOException, ServletException {
        Mockito.when(request.getHeaders(anyString())).thenReturn( new Vector<>(Arrays.asList("value1")).elements() );
        
        FilterChain test = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                try {
                    HttpHeaderCallback callback = new HttpHeaderCallback("header");
                    JaasHttpCallbackHandlerFilter.CALLBACK_HANDLER.handle(callback, null);
                    
                    assertThat(callback.isMultiValued()).isFalse();
                    assertThat(callback.getValue()).hasValue("value1");
                    assertThat(callback.getValues()).hasSize(1).contains("value1");
                } catch (UnsupportedCallbackException e) {
                    fail("Failure on handle() call", e);
                }
            }
        };
        
        filter.doFilter(request, response, test);
    }
    
    @Test
    public void testExistingHeaders() throws IOException, ServletException {
        Mockito.when(request.getHeaders(anyString())).thenReturn( new Vector<>(Arrays.asList("value1", "value2")).elements() );
        
        FilterChain test = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                try {
                    HttpHeaderCallback callback = new HttpHeaderCallback("header");
                    JaasHttpCallbackHandlerFilter.CALLBACK_HANDLER.handle(callback, null);
                    
                    assertThat(callback.isMultiValued()).isTrue();
                    assertThat(callback.getValue()).hasValue("value1,value2");
                    assertThat(callback.getValues()).hasSize(2).contains("value1", "value2");
                } catch (UnsupportedCallbackException e) {
                    fail("Failure on handle() call", e);
                }
            }
        };
        
        filter.doFilter(request, response, test);
    }
    
    @Test
    public void testMissingHeader() throws IOException, ServletException {
        Mockito.when(request.getHeaders(anyString())).thenReturn( new Vector<String>().elements() );
        
        FilterChain test = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                try {
                    HttpHeaderCallback callback = new HttpHeaderCallback("header");
                    JaasHttpCallbackHandlerFilter.CALLBACK_HANDLER.handle(callback, null);
                    
                    assertThat(callback.isMultiValued()).isFalse();
                    assertThat(callback.getValue()).isNotPresent();
                    assertThat(callback.getValues()).isEmpty();;
                } catch (UnsupportedCallbackException e) {
                    fail("Failure on handle() call", e);
                }
            }
        };
        
        filter.doFilter(request, response, test);
    }
    
    @Test
    public void testExistingCookie() throws IOException, ServletException {
        Mockito.when(request.getCookies()).thenReturn( new Cookie[] { new Cookie("cookie", "value") } );
        
        FilterChain test = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                try {
                    HttpCookieCallback callback = new HttpCookieCallback("cookie");
                    JaasHttpCallbackHandlerFilter.CALLBACK_HANDLER.handle(callback, null);
                    
                    assertThat(callback.getCookie())
                        .containsInstanceOf(Cookie.class)
                        .hasValueSatisfying(cookie -> assertThat(cookie.getValue()).isEqualTo("value"));
                } catch (UnsupportedCallbackException e) {
                    fail("Failure on handle() call", e);
                }
            }
        };
        
        filter.doFilter(request, response, test);
    }
    
    @Test
    public void testMissiongCookie() throws IOException, ServletException {
        Mockito.when(request.getCookies()).thenReturn( new Cookie[] { new Cookie("bogus", "") } );
        
        FilterChain test = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                try {
                    HttpCookieCallback callback = new HttpCookieCallback("cookie");
                    JaasHttpCallbackHandlerFilter.CALLBACK_HANDLER.handle(callback, null);
                    
                    assertThat(callback.getCookie()).isEmpty();
                } catch (UnsupportedCallbackException e) {
                    fail("Failure on handle() call", e);
                }
            }
        };
        
        filter.doFilter(request, response, test);
    }

}
