package com.thinkbiganalytics.sla.model;

import java.util.List;

/**
 * Created by sr186054 on 7/23/17.
 */
public class ObligationAssessment extends DefaultAssessment {

    public List<DefaultAssessment> metricAssessments;

    public List<DefaultAssessment> getMetricAssessments() {
        return metricAssessments;
    }

    public void setMetricAssessments(List<DefaultAssessment> metricAssessments) {
        this.metricAssessments = metricAssessments;
    }
}
