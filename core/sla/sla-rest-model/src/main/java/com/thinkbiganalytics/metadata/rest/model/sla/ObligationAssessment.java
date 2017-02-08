/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

/*-
 * #%L
 * thinkbig-sla-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObligationAssessment {

    private Obligation obligation;
    private Result result;
    private String message;
    private List<MetricAssessment> metricAssessments;

    public ObligationAssessment() {
        this.metricAssessments = new ArrayList<>();
    }

    public ObligationAssessment(Obligation obligation, Result result, String message) {
        this(obligation, result, message, new ArrayList<MetricAssessment>());
    }

    public ObligationAssessment(Obligation obligation, Result result, String message,
                                List<MetricAssessment> metricAssessments) {
        super();
        this.obligation = obligation;
        this.result = result;
        this.message = message;
        this.metricAssessments = metricAssessments;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MetricAssessment> getMetricAssessments() {
        return metricAssessments;
    }

    public void setMetricAssessments(List<MetricAssessment> metricAssessments) {
        this.metricAssessments = metricAssessments;
    }

    public void addMetricAssessment(MetricAssessment am) {
        this.metricAssessments.add(am);
    }

}
