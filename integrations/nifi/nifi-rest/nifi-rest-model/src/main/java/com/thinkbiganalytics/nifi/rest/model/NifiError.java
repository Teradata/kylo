package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

/**
 * Error status information when dealing with NiFi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiError {

    /**
     * the error message
     **/
    private String message;
    /**
     * a string allowing for grouping of errors together
     **/
    private String category;
    private SEVERITY severity;

    public NifiError() {

    }


    public NifiError(SEVERITY severity, String message) {
        this.severity = SEVERITY.WARN;
        this.message = message;
    }

    public NifiError(String message) {

        this.severity = SEVERITY.WARN;
        this.message = message;
    }

    public NifiError(SEVERITY severity, String message, String category) {
        this.severity = severity;
        this.message = message;
        this.category = category;
    }

    @JsonIgnore
    public boolean isFatal() {
        return SEVERITY.FATAL.equals(this.severity);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Return a string used to group errors together
     *
     * @return a string used to group errors together
     */
    public String getCategory() {
        return category;
    }

    /**
     * set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    public SEVERITY getSeverity() {
        return severity;
    }

    public void setSeverity(SEVERITY severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("severity", severity)
            .add("category", category)
            .add("message", message)
            .toString();
    }

    public enum SEVERITY {
        INFO(0), WARN(1), FATAL(2);

        private int level;

        SEVERITY(int level) {
            this.level = level;
        }
    }
}
