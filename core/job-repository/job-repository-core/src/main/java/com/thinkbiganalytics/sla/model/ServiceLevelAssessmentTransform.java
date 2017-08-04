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

import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.sla.model.DefaultAssessment;
import com.thinkbiganalytics.sla.model.DefaultServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.util.stream.Collectors;


public class ServiceLevelAssessmentTransform {


    public static DefaultServiceLevelAssessment toModel(ServiceLevelAssessment assessment){
        DefaultServiceLevelAssessment model = new DefaultServiceLevelAssessment();
        model.setAgreementName(assessment.getAgreement().getName());
        model.setAgreementId(assessment.getServiceLevelAgreementId() != null ? assessment.getServiceLevelAgreementId().toString(): null);
        if(assessment.getObligationAssessments() != null){
            model.setObligationAssessments(assessment.getObligationAssessments().stream().map(obligationAssessment -> toModel(obligationAssessment)).collect(Collectors.toList()));
        }
        return model;
    }

    private static com.thinkbiganalytics.sla.model.ObligationAssessment toModel(ObligationAssessment obligationAssessment){
        com.thinkbiganalytics.sla.model.ObligationAssessment assessment = new com.thinkbiganalytics.sla.model.ObligationAssessment();
        assessment.setMessage(obligationAssessment.getMessage());
        assessment.setResult(obligationAssessment.getResult().name());
        if(assessment.getMetricAssessments() != null){
            assessment.setMetricAssessments(obligationAssessment.getMetricAssessments().stream().map(metricAssessment -> toModel(metricAssessment)).collect(Collectors.toList()));
        }
        return assessment;
    }

    private static DefaultAssessment toModel(MetricAssessment metricAssessment){
        DefaultAssessment assessment = new DefaultAssessment();
        assessment.setMessage(metricAssessment.getMessage());
        assessment.setResult(metricAssessment.getResult().name());
        return assessment;
    }
}
