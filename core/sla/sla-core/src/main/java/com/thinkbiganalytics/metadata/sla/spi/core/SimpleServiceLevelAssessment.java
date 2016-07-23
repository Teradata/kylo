/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import com.google.common.collect.ComparisonChain;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ServiceLevelAgreement getAgreement() {
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
    
    @Override
    public int compareTo(ServiceLevelAssessment sla) {
        ComparisonChain chain = ComparisonChain
                .start()
                .compare(this.getResult(), sla.getResult());
                //.compare(this.getAgreement().getName(), sla.getAgreement().getName());
        
        if (chain.result() != 0) {
            return chain.result();
        }
        
        List<ObligationAssessment> list1 = new ArrayList<>(this.getObligationAssessments());
        List<ObligationAssessment> list2 = new ArrayList<>(sla.getObligationAssessments());
        
        chain = chain.compare(list1.size(), list2.size());
    
        Collections.sort(list1);
        Collections.sort(list2);
        
        for (int idx = 0; idx < list1.size(); idx++) {
            chain = chain.compare(list1.get(idx), list2.get(idx));
        }
        
        return chain.result();
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
