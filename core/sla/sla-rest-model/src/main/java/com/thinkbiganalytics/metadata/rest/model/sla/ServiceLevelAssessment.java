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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAssessment {

    private String id;
    List<ObligationAssessment> obligationAssessments;
    @JsonSerialize(using = DateTimeSerializer.class)
    private DateTime time;

    private ServiceLevelAgreement agreement;
    private String message;
    private Result result;

    public ServiceLevelAssessment() {
        this.obligationAssessments = new ArrayList<>();
    }

    public ServiceLevelAssessment(ServiceLevelAgreement agreement, DateTime time, String message, Result result) {
        this(agreement, time, message, result, new ArrayList<ObligationAssessment>());
    }

    public ServiceLevelAssessment(ServiceLevelAgreement agreement, DateTime time, String message, Result result,
                                  List<ObligationAssessment> obligationAssessments) {
        super();
        this.agreement = agreement;
        this.time = time;
        this.message = message;
        this.result = result;
        this.obligationAssessments = obligationAssessments;
    }

    public ServiceLevelAgreement getAgreement() {
        return agreement;
    }

    public void setAgreement(ServiceLevelAgreement agreement) {
        this.agreement = agreement;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public List<ObligationAssessment> getObligationAssessments() {
        return obligationAssessments;
    }

    public void setObligationAssessments(List<ObligationAssessment> obligationAssessments) {
        this.obligationAssessments = obligationAssessments;
    }

    public void addObligationAssessment(ObligationAssessment oa) {
        this.obligationAssessments.add(oa);
    }

    public enum Result {SUCCESS, WARNING, FAILURE}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
