package com.thinkbiganalytics.kylo.spark.model.enums;

/*-
 * #%L
 * kylo-spark-livy-server
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


import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Session State
 * Value	   Description
 * not_started	   Session has not been started
 * starting	   Session is starting
 * recovering      ??  see: LIVY-496
 * idle	           Session is waiting for input
 * running         ??  see: LIVY-496
 * busy	           Session is executing a statement
 * shutting_down   Session is shutting down
 * error	   Session errored out
 * dead	           Session has exited
 * killed          ??: see: LIVY-496
 * success	   Session is successfully stopped
 */
public enum SessionState {
    not_started,
    starting,
    recovering,
    idle,
    running,
    busy,
    shutting_down,
    error,
    dead,
    killed,
    success;

    public final static Set<SessionState> FINAL_STATES = Sets.immutableEnumSet(dead, killed, error, success);
    public final static Set<SessionState> READY_STATES = Sets.immutableEnumSet(starting, recovering, idle, running, busy);

}
