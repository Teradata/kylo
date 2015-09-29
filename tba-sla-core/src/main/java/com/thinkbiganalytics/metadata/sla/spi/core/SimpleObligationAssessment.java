/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

/**
 *
 * @author Sean Felten
 */
public class SimpleObligationAssessment implements ObligationAssessment {

    private Obligation obligation;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private Set<MetricAssessment> metricAssessments;

    /**
     * 
     */
    protected SimpleObligationAssessment() {
        this.metricAssessments = new HashSet<MetricAssessment>();
    }
    
    public SimpleObligationAssessment(Obligation obligation) {
        this();
        this.obligation = obligation;
    }
    
    public SimpleObligationAssessment(Obligation obligation, String message, AssessmentResult result) {
        this();
        this.obligation = obligation;
        this.message = message;
        this.result = result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getObligation()
     */
    @Override
    public Obligation getObligation() {
        return this.obligation;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getMessage()
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getResult()
     */
    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getMetricAssessments()
     */
    @Override
    public Set<MetricAssessment> getMetricAssessments() {
        return new HashSet<MetricAssessment>(this.metricAssessments);
    }
    
    protected boolean add(MetricAssessment assessment) {
        return this.metricAssessments.add(assessment);
    }
    
    protected boolean addAll(Collection<? extends MetricAssessment> assessments) {
        return this.metricAssessments.addAll(assessments);
    }

    protected void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    protected void setMetricAssessments(Set<MetricAssessment> metricAssessments) {
        this.metricAssessments = metricAssessments;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
    }

    
}
