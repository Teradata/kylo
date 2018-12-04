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

import java.io.Serializable;
import java.util.Optional;

import javax.security.auth.callback.Callback;
import javax.servlet.http.Cookie;

/**
 * A callback for obtaining a particular cookie from the authenticating HTTP request.
 */
public class HttpCookieCallback implements Callback, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final boolean ignoreCase;
    private Cookie cookie;
    
    /**
     * Creates a callback for requesting a particular cookie by name (case-sensitive).
     * @param cookie the cookie name to match
     */
    public HttpCookieCallback(String cookie) {
        this(cookie, false);
    }
    
    /**
     * Creates a callback for requesting a particular cookie by name (case-sensitive).
     * @param cookie the cookie name to match
     */
    public HttpCookieCallback(String cookie, boolean ignoreCase) {
        this.name = cookie;
        this.ignoreCase = ignoreCase;
    }
    
    /**
     * @return the name of the cookie to find in the request
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the ignoreCase true if the cookie name comparison should ignore case
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * Supplies the optional cookie if one is found in the request matching the 
     * cookie name.
     * @return the optional cookie
     */
    public Optional<Cookie> getCookie() {
        return Optional.ofNullable(cookie);
    }
    
    /**
     * @param cookie the matching cookie found
     */
    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }
}
