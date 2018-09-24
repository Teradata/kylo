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

import org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter captures the request for the current thread and exports a CallbackHandler that can provide
 * headers and cookies to LoginModules, using supported Callbacks, extracted from that request.
 */
public class JaasHttpCallbackHandlerFilter extends GenericFilterBean {

    public static final JaasAuthenticationCallbackHandler CALLBACK_HANDLER = new HttpAuthenticationCallbackHandler();
    
    private static final ThreadLocal<HttpCallbackContext> contextHolder = new ThreadLocal<>();

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            capture(request, response);
            chain.doFilter(request, response);
            return;
        } finally {
            release();
        }

    }
    
    protected void capture(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        contextHolder.set(new HttpCallbackContext(httpReq, httpResp));
    }
    
    protected void release() {
        contextHolder.remove();
    }
    
    protected static class HttpAuthenticationCallbackHandler implements JaasAuthenticationCallbackHandler {
        @Override
        public void handle(Callback callback, Authentication auth) throws IOException, UnsupportedCallbackException {
            HttpCallbackContext context = contextHolder.get();
            if (context == null) {
                return;
            } else if (callback instanceof HttpHeaderCallback) {
                HttpHeaderCallback headerCallback = (HttpHeaderCallback) callback;
                handleHeader(context, headerCallback);
            } else if (callback instanceof HttpCookieCallback) {
                HttpCookieCallback cookieCallback = (HttpCookieCallback) callback;
                handleCookie(context, cookieCallback);
            }
        }

        protected void handleCookie(HttpCallbackContext context, HttpCookieCallback callback) {
            Cookie[] cookies = context.request.getCookies();
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    String name = cookie.getName();
                    boolean match = callback.isIgnoreCase() ? name.equalsIgnoreCase(callback.getName()) : name.equals(callback.getName());
                    
                    if (match) {
                        callback.setCookie(cookie);
                        break;
                    }
                }
            }
        }

        protected void handleHeader(HttpCallbackContext context, HttpHeaderCallback callback) {
            Enumeration<String> hdrEnum = context.request.getHeaders(callback.getName());
            callback.setValues(Collections.list(hdrEnum));
        }
    }

    protected static class HttpCallbackContext {
        private HttpServletRequest request;
//        private HttpServletResponse response;
        
        public HttpCallbackContext(HttpServletRequest req, HttpServletResponse resp) {
            this.request = req;
//            this.response = resp;
        }
    }
}
