/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.common;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.nio.file.Path;

/**
 * Defines and resolves paths of users and user groups within the JCR repository.
 */
public interface UsersPaths {

    public static final Path USERS = JcrUtil.path("users");
    public static final Path GROUPS = JcrUtil.path("groups");

    static Path userPath(String name) {
        return USERS.resolve(name);
    }

    static Path groupPath(String name) {
        return GROUPS.resolve(name);
    }
}
