/**
 *
 */
package com.thinkbiganalytics.metadata.api.sla;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 * FOR TESTING ONLY.  This metric will always be assessed with the result it has been set on it.
 */
public abstract class TestMetric implements Metric {

    private static final long serialVersionUID = 1L;

    private AssessmentResult result;
    private String message;

    public TestMetric() {
    }

    public TestMetric(AssessmentResult result) {
        this(result, "This metric will always assess with the result: " + result);
    }

    public TestMetric(AssessmentResult result, String msg) {
        super();
        this.result = result;
        this.message = msg;
    }

    public AssessmentResult getResult() {
        return result;
    }

    public void setResult(AssessmentResult result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
