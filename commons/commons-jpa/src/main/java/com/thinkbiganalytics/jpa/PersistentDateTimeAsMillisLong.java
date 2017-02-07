package com.thinkbiganalytics.jpa;

/*-
 * #%L
 * thinkbig-commons-jpa
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

import org.jadira.usertype.spi.shared.AbstractVersionableUserType;
import org.joda.time.DateTime;

/**
 */
public class PersistentDateTimeAsMillisLong extends AbstractVersionableUserType<DateTime, Long, LongColumnDateTimeMapper> {

    private static final long serialVersionUID = 2654706404517200613L;

    public PersistentDateTimeAsMillisLong() {
    }

    public int compare(Object o1, Object o2) {
        return ((DateTime) o1).compareTo((DateTime) o2);
    }
}
