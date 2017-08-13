package com.thinkbiganalytics.metadata.rest.model.sla;

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


import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;


public class ServiceLevelAssessmentTransform {


    public static ServiceLevelAssessment toModel(com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assessment){
        ServiceLevelAssessment model = new ServiceLevelAssessment();
        model.setAgreement(new ServiceLevelAgreement());
        if(assessment.getServiceLevelAgreementDescription() != null){
            if(StringUtils.isNotBlank(assessment.getServiceLevelAgreementDescription().getName())){
                model.getAgreement().setName(assessment.getServiceLevelAgreementDescription().getName());
            }
            if(StringUtils.isNotBlank(assessment.getServiceLevelAgreementDescription().getDescription())){
                model.getAgreement().setDescription(assessment.getServiceLevelAgreementDescription().getDescription());
            }
            if(assessment.getServiceLevelAgreementDescription().getSlaId() != null){
                model.getAgreement().setId(assessment.getServiceLevelAgreementDescription().getSlaId().toString());
            }
        }

        if(model.getAgreement().getId() == null){
            model.getAgreement().setId(assessment.getServiceLevelAgreementId() != null ? assessment.getServiceLevelAgreementId().toString(): null);
        }
        if(model.getAgreement().getName() == null & assessment.getAgreement() != null){
            model.getAgreement().setName(assessment.getAgreement().getName());
        }



        if(assessment.getObligationAssessments() != null){
            model.setObligationAssessments(assessment.getObligationAssessments().stream().map(obligationAssessment -> toModel(obligationAssessment)).collect(Collectors.toList()));
        }
        model.setMessage(assessment.getMessage());
        model.setResult(ServiceLevelAssessment.Result.valueOf(assessment.getResult().name()));
        model.setTime(assessment.getTime());
        model.setId(assessment.getId().toString());
        return model;
    }

    private static ObligationAssessment  toModel(com.thinkbiganalytics.metadata.sla.api.ObligationAssessment obligationAssessment){
        ObligationAssessment assessment = new ObligationAssessment();
        assessment.setMessage(obligationAssessment.getMessage());
        assessment.setResult(ServiceLevelAssessment.Result.valueOf(obligationAssessment.getResult().name()));
        if(obligationAssessment.getObligation() != null) {
            Obligation obligation = new Obligation();
            obligation.setDescription(obligationAssessment.getObligation().getDescription());
        }
        if(assessment.getMetricAssessments() != null){
            assessment.setMetricAssessments(obligationAssessment.getMetricAssessments().stream().map(metricAssessment -> toModel(metricAssessment)).collect(Collectors.toList()));
        }
        return assessment;
    }

    private static MetricAssessment toModel(com.thinkbiganalytics.metadata.sla.api.MetricAssessment metricAssessment){
        MetricAssessment assessment = new MetricAssessment();
        assessment.setMessage(metricAssessment.getMessage());
        assessment.setResult(ServiceLevelAssessment.Result.valueOf(metricAssessment.getResult().name()));
        if(metricAssessment.getMetric() != null) {
            assessment.setMetric(metricAssessment.getMetric());
        }
        return assessment;
    }

}
