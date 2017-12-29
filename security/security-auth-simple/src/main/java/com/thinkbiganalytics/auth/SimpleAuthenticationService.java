package com.thinkbiganalytics.auth;

/*-
 * #%L
 * thinkbig-security-auth-simple
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

import java.util.Arrays;

/**
 * Simple Auth to compare passed in Username and Password
 * username and passwords are passed in via application.properties
 */
public class SimpleAuthenticationService implements AuthenticationService {


    protected String username;

    protected char[] password;

    public boolean authenticate(String username, char[] password) {
        if (username.equalsIgnoreCase(this.username) && Arrays.equals(password, this.password)) {
            return true;
        }
        return false;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
