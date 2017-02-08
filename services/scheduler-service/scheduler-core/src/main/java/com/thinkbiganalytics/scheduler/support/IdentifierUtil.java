package com.thinkbiganalytics.scheduler.support;

/*-
 * #%L
 * thinkbig-scheduler-core
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

import java.util.UUID;

/**
 * Creates a unique name
 */
public class IdentifierUtil {

    public static String createUniqueName(String item) {

        String n1 = UUID.randomUUID().toString();
        String n2 = UUID.nameUUIDFromBytes(item.getBytes()).toString();
        return String.format("%s-%s", new Object[]{n2.substring(24), n1});
    }

    public static String createUniqueName(String item, int length) {

        String n1 = UUID.randomUUID().toString();
        String n2 = UUID.nameUUIDFromBytes(item.getBytes()).toString();
        return String.format("%s-%s", new Object[]{n2.substring(length), n1});
    }
}

