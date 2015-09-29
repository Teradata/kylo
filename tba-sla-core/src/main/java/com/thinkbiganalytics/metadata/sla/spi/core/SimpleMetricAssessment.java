/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;

/**
 *
 * @author Sean Felten
 */
public class SimpleMetricAssessment implements MetricAssessment {

    private Metric metric;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    
    /**
     * 
     */
    protected SimpleMetricAssessment() {
        super();
    }
    
    public SimpleMetricAssessment(Metric metric) {
        this();
        this.metric = metric;
    }
    
    public SimpleMetricAssessment(Metric metric, String message, AssessmentResult result) {
        this();
        this.metric = metric;
        this.message = message;
        this.result = result;
    }

    @Override
    public Metric getMetric() {
        return this.metric;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    protected void setMetric(Metric metric) {
        this.metric = metric;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
    }

    
}
