package com.thinkbiganalytics.nifi.v2.spark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*-
 * #%L
 * thinkbig-nifi-spark-service
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
public class TimeoutContext {

    public String ContextName;
    private int timeoutSeconds;
    private long timeoutTime;
    public boolean isRunning;
    private List<String> executionLocks = new ArrayList<String>();

    public TimeoutContext(String contextName) {
        this.ContextName = contextName;
        this.timeoutSeconds = 0;
        isRunning = false;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        resetTimeoutTime();
    }

    public void resetTimeoutTime() {
        this.timeoutTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
    }

    public boolean hasTimedOut() {
        if (timeoutTime != 0 && System.nanoTime() > timeoutTime) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isLocked() {
        if (!executionLocks.isEmpty()) return true;
        else return false;
    }

    public void addExecutionLock(String id) {
        executionLocks.add(id);
    }

    public void removeExecutionLock(String id) {
        executionLocks.remove(id);
    }
}