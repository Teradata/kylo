/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
public class SimpleServiceLevelAssessment implements ServiceLevelAssessment {
    
    private static final long serialVersionUID = 2752726049105871947L;
    
    private DateTime time;
    private ServiceLevelAgreement sla;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private Set<ObligationAssessment> obligationAssessments;
    
    /**
     * 
     */
    protected SimpleServiceLevelAssessment() {
        this.time = DateTime.now();
        this.obligationAssessments = new HashSet<ObligationAssessment>();
    }

    public SimpleServiceLevelAssessment(ServiceLevelAgreement sla) {
        this();
        this.sla = sla;
    }
    
    public SimpleServiceLevelAssessment(ServiceLevelAgreement sla, String message, AssessmentResult result) {
        super();
        this.sla = sla;
        this.message = message;
        this.result = result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getTime()
     */
    @Override
    public DateTime getTime() {
        return this.time;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getSLA()
     */
    @Override
    public ServiceLevelAgreement getSLA() {
        return this.sla;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getMessage()
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getResult()
     */
    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getObligationAssessments()
     */
    @Override
    public Set<ObligationAssessment> getObligationAssessments() {
        return new HashSet<ObligationAssessment>(this.obligationAssessments);
    }
    
    protected boolean add(ObligationAssessment assessment) {
        return this.obligationAssessments.add(assessment);
    }
    
    protected boolean addAll(Collection<? extends ObligationAssessment> assessments) {
        return this.obligationAssessments.addAll(assessments);
    }

    protected void setTime(DateTime time) {
        this.time = time;
    }

    protected void setSla(ServiceLevelAgreement sla) {
        this.sla = sla;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
    }

    
}
