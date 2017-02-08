/**
 *
 */
package com.thinkbiganalytics.metadata.api.user;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 * Thrown when there is an attempt to create a user when one already exists with the same name.
 */
public class UserAlreadyExistsException extends MetadataException {

    private static final long serialVersionUID = 1L;

    private final String username;

    /**
     * @param username
     */
    public UserAlreadyExistsException(String username) {
        super("A user with the name \"" + username + "\" already exists");
        this.username = username;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}
