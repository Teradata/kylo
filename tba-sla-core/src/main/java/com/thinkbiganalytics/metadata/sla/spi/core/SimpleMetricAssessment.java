/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import java.io.Serializable;
import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleObligationAssessment.DefaultComparator;

/**
 *
 * @author Sean Felten
 */
public class SimpleMetricAssessment implements MetricAssessment {
    
    private static final long serialVersionUID = -209788646749034842L;
    
    public static final Comparator<MetricAssessment> DEF_COMPARATOR = new DefaultComparator();
    
    private Metric metric;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private Comparator<MetricAssessment> comparator = DEF_COMPARATOR;
    private Comparable<? extends Serializable>[] comparables;
    
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

    @Override
    public int compareTo(MetricAssessment metric) {
        return this.comparator.compare(this, metric);
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
    
    protected void setComparator(Comparator<MetricAssessment> comparator) {
        this.comparator = comparator;
    }
    
    protected void setComparables(Comparable<? extends Serializable>[] comparables) {
        this.comparables = comparables;
    }

    protected static class DefaultComparator implements Comparator<MetricAssessment> {
        @Override
        public int compare(MetricAssessment o1, MetricAssessment o2) {
            ComparisonChain chain = ComparisonChain
                    .start()
                    .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof SimpleMetricAssessment && o2 instanceof SimpleMetricAssessment) {
                SimpleMetricAssessment s1 = (SimpleMetricAssessment) o1;
                SimpleMetricAssessment s2 = (SimpleMetricAssessment) o2;
                
                for (int idx = 0; idx < s1.comparables.length; idx++) {
                    chain = chain.compare(s1.comparables[idx], s2.comparables[idx]);
                }
            }
            
            return chain.result();
        }
    }
    
}
