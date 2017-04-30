package com.thinkbiganalytics.nifi.v2.spark;

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
    private int reapTimeoutSeconds;
    private long reapTime;

    public TimeoutContext(String contextName, int reapTimeoutSeconds) {
        this.ContextName = contextName;
        this.reapTimeoutSeconds = reapTimeoutSeconds;
        resetTimeout();
    }

    public void resetTimeout() {
        reapTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(reapTimeoutSeconds);
    }

    public boolean hasTimedOut() {
        if (System.nanoTime() > reapTime) {
            return true;
        } else {
            return false;
        }
    }

    public long getTimeoutTime() {
        return reapTime;
    }
}