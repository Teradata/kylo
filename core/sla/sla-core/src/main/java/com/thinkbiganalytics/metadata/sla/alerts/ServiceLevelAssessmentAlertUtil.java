package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * Created by sr186054 on 7/20/16.
 */
public class ServiceLevelAssessmentAlertUtil {


    public static String getDescription(ServiceLevelAssessment slaAssmt) {

        StringBuilder descrBldr = new StringBuilder();

        // Generate a description string for the issue in outline form using the results and messages
        // of the assessment components: sla assessment->obligation assessments->metric assessments
        for (ObligationAssessment obAssmnt : slaAssmt.getObligationAssessments()) {
            for (MetricAssessment metricAssmnt : obAssmnt.getMetricAssessments()) {
                descrBldr
                    .append("Requirement: ")
                    .append(metricAssmnt.getMetric().getDescription())
                    .append("\n\nResult: ")
                    .append(metricAssmnt.getMessage())
                    .append("\n\n");
            }
        }
        descrBldr.append("\nAssessed on ").append(slaAssmt.getTime());

        return descrBldr.toString();
    }

}
