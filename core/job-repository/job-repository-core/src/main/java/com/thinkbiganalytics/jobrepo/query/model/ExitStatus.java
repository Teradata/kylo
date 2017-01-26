package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import java.io.Serializable;

public class ExitStatus implements Serializable, Comparable<ExitStatus> {

    public static final ExitStatus UNKNOWN = new ExitStatus("UNKNOWN");
    public static final ExitStatus EXECUTING = new ExitStatus("EXECUTING");
    public static final ExitStatus COMPLETED = new ExitStatus("COMPLETED");
    public static final ExitStatus NOOP = new ExitStatus("NOOP");
    public static final ExitStatus FAILED = new ExitStatus("FAILED");
    public static final ExitStatus STOPPED = new ExitStatus("STOPPED");
    private final String exitCode;
    private final String exitDescription;

    public ExitStatus(String exitCode) {
        this(exitCode, "");
    }

    public ExitStatus(String exitCode, String exitDescription) {
        this.exitCode = exitCode;
        this.exitDescription = exitDescription == null ? "" : exitDescription;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getExitDescription() {
        return exitDescription;
    }

    public String toString() {
        return String.format("exitCode=%s;exitDescription=%s", new Object[]{this.exitCode, this.exitDescription});
    }

    public boolean equals(Object obj) {
        return obj == null ? false : this.toString().equals(obj.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    private int severity(ExitStatus status) {
        return status.exitCode.startsWith(EXECUTING.exitCode) ? 1 : (status.exitCode.startsWith(COMPLETED.exitCode) ? 2 : (
            status.exitCode.startsWith(NOOP.exitCode) ? 3 : (status.exitCode.startsWith(STOPPED.exitCode) ? 4 : (
                status.exitCode.startsWith(FAILED.exitCode) ? 5 : (status.exitCode.startsWith(UNKNOWN.exitCode) ? 6 : 7)))));
    }

    public int compareTo(ExitStatus status) {
        return this.severity(status) > this.severity(this) ? -1 : (this.severity(status) < this.severity(this) ? 1
                                                                                                               : this.getExitCode()
                                                                       .compareTo(status.getExitCode()));
    }

}
