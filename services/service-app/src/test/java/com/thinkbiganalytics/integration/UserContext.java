package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-service-app
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
import java.util.Optional;

/**
 * Created by ru186002 on 09/05/2017.
 */
public class UserContext {

    private static UserContext.User user;

    public enum User {
        ADMIN("dladmin", "thinkbig"),
        ANALYST("analyst", "analyst");

        private final String username;
        private final String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public static User fromName(String name) {
            Optional<User> first = Arrays.stream(User.values()).filter(user1 -> user1.username.equals(name)).findFirst();
            if (!first.isPresent()) {
                throw new IllegalStateException("Unknown user name " + name);
            }
            return first.get();
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
    public static void setUser(User user) {
        UserContext.user = user;
    }

    public static User getUser() {
        return UserContext.user;
    }

    public static void reset() {
        UserContext.user = User.ADMIN;
    }
}
