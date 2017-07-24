package com.thinkbiganalytics.sla.model;

import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.sla.model.DefaultAssessment;
import com.thinkbiganalytics.sla.model.DefaultServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.util.stream.Collectors;

/**
 * Created by sr186054 on 7/23/17.
 */
public class ServiceLevelAssessmentTransform {


    public static DefaultServiceLevelAssessment toModel(ServiceLevelAssessment assessment){
        DefaultServiceLevelAssessment model = new DefaultServiceLevelAssessment();
        model.setAgreementName(assessment.getAgreement().getName());
        model.setAgreementId(assessment.getServiceLevelAgreementId());
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
