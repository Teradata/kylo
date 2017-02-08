/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api;

/*-
 * #%L
 * thinkbig-sla-api
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

/**
 * Thrown when there is a problem when attempting to assess an SLA.
 */
public class ServiceLevelAssessmentException extends RuntimeException {

    private static final long serialVersionUID = 7236695892099702703L;

    public ServiceLevelAssessmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceLevelAssessmentException(String message) {
        super(message);
    }
}
