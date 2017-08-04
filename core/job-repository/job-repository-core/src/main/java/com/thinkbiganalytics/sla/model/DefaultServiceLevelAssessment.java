package com.thinkbiganalytics.sla.model;

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

import org.joda.time.DateTime;

import java.util.List;


public class DefaultServiceLevelAssessment {


    private String agreementName;
    private String agreementId;

    private String description;
    private DateTime createdTime;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    private DefaultAssessment assessment;

    private List<ObligationAssessment> obligationAssessments;


    public String getAgreementName() {
        return agreementName;
    }

    public void setAgreementName(String agreementName) {
        this.agreementName = agreementName;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DefaultAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(DefaultAssessment assessment) {
        this.assessment = assessment;
    }

    public List<ObligationAssessment> getObligationAssessments() {
        return obligationAssessments;
    }

    public void setObligationAssessments(List<ObligationAssessment> obligationAssessments) {
        this.obligationAssessments = obligationAssessments;
    }
}
