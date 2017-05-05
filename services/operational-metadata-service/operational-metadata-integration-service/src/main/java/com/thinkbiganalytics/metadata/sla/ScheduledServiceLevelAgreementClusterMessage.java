package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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


import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.scheduler.JobIdentifier;

import java.io.Serializable;

/**
 * A cluster message for SLA schedules
 */
public class ScheduledServiceLevelAgreementClusterMessage implements Serializable {

    private ServiceLevelAgreement.ID slaId;

    private JobIdentifier jobIdentifier;

    public ScheduledServiceLevelAgreementClusterMessage(){

    }

    public ScheduledServiceLevelAgreementClusterMessage(ServiceLevelAgreement.ID slaId, JobIdentifier jobIdentifier){
        this.slaId = slaId;
        this.jobIdentifier = jobIdentifier;
    }

    public ServiceLevelAgreement.ID getSlaId() {
        return slaId;
    }

    public void setSlaId(ServiceLevelAgreement.ID slaId) {
        this.slaId = slaId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }
}
