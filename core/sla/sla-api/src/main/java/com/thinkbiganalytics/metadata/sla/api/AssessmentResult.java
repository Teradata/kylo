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
 * The possible result of an assessment of an SLA, obligation, or metic.
 */
public enum AssessmentResult {
    SUCCESS, WARNING, FAILURE;

    /**
     * Returns whether this result or the argument is a more severe result (i.e. which is closer to failure)
     *
     * @param result the result to be compared
     * @return this result or the argument depending upon which is more severe of a result
     */
    public AssessmentResult max(AssessmentResult result) {
        return result.ordinal() > this.ordinal() ? result : this;
    }

    /**
     * Returns whether this result or the argument is a less severe result (i.e. which is closer to failure)
     *
     * @param result the result to be compared
     * @return this result or the argument depending upon which is more severe of a result
     */
    public AssessmentResult min(AssessmentResult result) {
        return result.ordinal() < this.ordinal() ? result : this;
    }
}
