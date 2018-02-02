package com.thinkbiganalytics.nifi.v2.core.savepoint;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
/**
 * Lock object to guarantee only one process is operating on a savepoint at a time
 */
public class Lock {

    @JsonProperty
    private String savepointId;

    @JsonProperty
    private Long time = System.currentTimeMillis();

    public boolean isExpired(long maxLockTime) {
        return ((System.currentTimeMillis() - time) > maxLockTime);
    }

    public String getSavepointId() {
        return savepointId;
    }

    public Lock(String savepointId) {
        this.savepointId = savepointId;
    }

    public Lock() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lock)) {
            return false;
        }

        Lock lock = (Lock) o;

        return time != null ? time.equals(lock.time) : lock.time == null;
    }

    @Override
    public int hashCode() {
        return time != null ? time.hashCode() : 0;
    }
}
