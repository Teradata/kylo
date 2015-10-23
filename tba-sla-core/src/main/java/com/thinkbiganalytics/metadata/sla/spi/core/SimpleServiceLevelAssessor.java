/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.AssessorNotFoundException;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

/**
 *
 * @author Sean Felten
 */
public class SimpleServiceLevelAssessor implements ServiceLevelAssessor {

    private Set<ObligationAssessor<? extends Obligation>> obligationAssessors;
    private Set<MetricAssessor<? extends Metric>> metricAssessors;
    private ObligationAssessor<? extends Obligation> defaultObligationAssessor;

    /**
     * 
     */
    public SimpleServiceLevelAssessor() {
        this.obligationAssessors = Collections.synchronizedSet(new HashSet<ObligationAssessor<? extends Obligation>>());
        this.metricAssessors = Collections.synchronizedSet(new HashSet<MetricAssessor<? extends Metric>>());
        this.defaultObligationAssessor = createDefaultObligationAssessor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#assess(com.
     * thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement)
     */
    @Override
    public ServiceLevelAssessment assess(ServiceLevelAgreement sla) {
        Log.info("Assessing SLA: {}", sla.getName());
        
        SimpleServiceLevelAssessment slaAssessment = new SimpleServiceLevelAssessment(sla);
        AssessmentResult result = AssessmentResult.SUCCESS;
        
        for (Obligation ob : sla.getObligations()) {
            ObligationAssessmentBuilderImpl builder = new ObligationAssessmentBuilderImpl(ob);
            ObligationAssessor<Obligation> assessor = (ObligationAssessor<Obligation>) findAssessor(ob);
            
            Log.debug("Assessing obligation \"{}\" with assessor: {}", assessor);
            
            assessor.assess(ob, builder);
            
            ObligationAssessment obAssessment = builder.build();
            slaAssessment.add(obAssessment);
            result = result.max(obAssessment.getResult());
        }
        
        slaAssessment.setResult(result);
        if (result == AssessmentResult.SUCCESS) {
            slaAssessment.setMessage("SLA assessment requirements were met");
        } else {
            slaAssessment.setMessage("At least one of the SLA obligations resulted in the status: " + result);
        }
        
        Log.debug("Completed assessment of SLA {}: {}", sla.getName(), slaAssessment.getResult());
        return slaAssessment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#
     * registerObligationAssessor(com.thinkbiganalytics.metadata.sla.spi.
     * ObligationAssessor)
     */
    @Override
    public ObligationAssessor<? extends Obligation> registerObligationAssessor(ObligationAssessor<? extends Obligation> assessor) {
        this.obligationAssessors.add(assessor);
        return assessor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor#
     * registerMetricAssessor(com.thinkbiganalytics.metadata.sla.spi.
     * MetricAssessor)
     */
    @Override
    public MetricAssessor<? extends Metric> registerMetricAssessor(MetricAssessor<? extends Metric> assessor) {
        this.metricAssessors.add(assessor);
        return assessor;
    }

    protected ObligationAssessor<? extends Obligation> createDefaultObligationAssessor() {
        return new DefaultObligationAssessor();
    }

    
    protected ObligationAssessor<? extends Obligation> findAssessor(Obligation obligation) {
        synchronized (this.obligationAssessors) {
            for (ObligationAssessor<? extends Obligation> assessor : this.obligationAssessors) {
                if (assessor.accepts(obligation)) {
                    return assessor;
                }
            }
        }
        
        return this.defaultObligationAssessor;
    }
    
    @SuppressWarnings("unchecked")
    protected <M extends Metric> MetricAssessor<M> findAssessor(M metric) {
        synchronized (this.metricAssessors) {
            for (MetricAssessor<? extends Metric> accessor : this.metricAssessors) {
                if (accessor.accepts(metric)) {
                    return (MetricAssessor<M>) accessor;
                }
            }
        }
        throw new AssessorNotFoundException(metric);
    }

    
    private class ObligationAssessmentBuilderImpl implements ObligationAssessmentBuilder {

        private Obligation obligation;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private Comparator<ObligationAssessment> comparator;
        private List<Comparable<? extends Serializable>> comparables;

        private SimpleObligationAssessment assessment;
        
        public ObligationAssessmentBuilderImpl(Obligation obligation) {
            this.obligation = obligation;
            this.assessment = new SimpleObligationAssessment(obligation);
        }
        
        @Override
        public ObligationAssessmentBuilder obligation(Obligation ob) {
            this.obligation = ob;
            return this;
        }

        @Override
        public ObligationAssessmentBuilder result(AssessmentResult result) {
            this.result = result;
            return this;
        }

        @Override
        public ObligationAssessmentBuilder message(String descr) {
            this.message = descr;
            return this;
        }
        
        @Override
        public ObligationAssessmentBuilder comparator(Comparator<ObligationAssessment> comp) {
            this.comparator = comp;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public ObligationAssessmentBuilder compareWith(final Comparable<? extends Serializable> value, 
                                                       @SuppressWarnings("unchecked") 
                                                       final Comparable<? extends Serializable>... otherValeus) {
            ;
            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(value));
            this.comparables.addAll(Arrays.asList(value));
            return this;
        }

        @Override
        public <M extends Metric> MetricAssessment assess(M metric) {
            MetricAssessor<M> assessor = findAssessor(metric);
            MetricAssessmentBuilderImpl builder = new MetricAssessmentBuilderImpl(metric);

            assessor.assess(metric, builder);
            MetricAssessment metricAssmt = builder.build();
            this.assessment.add(metricAssmt);
            return metricAssmt;
        }
        
        protected ObligationAssessment build() {
            this.assessment.setObligation(this.obligation);
            this.assessment.setMessage(this.message);
            this.assessment.setResult(this.result);
            
            if (this.comparator != null) {
                this.assessment.setComparator(this.comparator);
            }
            
            if (this.comparables != null) {
                this.assessment.setComparables(this.comparables);
            }
            
            return this.assessment;
        }
    }
    
    private class MetricAssessmentBuilderImpl implements MetricAssessmentBuilder {
        
        private Metric metric;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private Comparator<MetricAssessment> comparator;
        private List<Comparable<? extends Serializable>> comparables;
        
        public MetricAssessmentBuilderImpl(Metric metric) {
            this.metric = metric;
        }

        @Override
        public MetricAssessmentBuilder metric(Metric metric) {
            this.metric = metric;
            return this;
        }

        @Override
        public MetricAssessmentBuilder message(String descr) {
            this.message = descr;
            return this;
        }

        @Override
        public MetricAssessmentBuilder result(AssessmentResult result) {
            this.result = result;
            return this;
        }
        
        @Override
        public MetricAssessmentBuilder comparitor(Comparator<MetricAssessment> comp) {
            this.comparator = comp;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetricAssessmentBuilder compareWith(Comparable<? extends Serializable> value, 
                                                   Comparable<? extends Serializable>... otherValues) {
            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(value));
            this.comparables.addAll(Arrays.asList(value));
            return this;
        }

        protected MetricAssessment build() {
            SimpleMetricAssessment assessment = new SimpleMetricAssessment(this.metric);
            assessment.setMessage(this.message);
            assessment.setResult(this.result);
            
            if (this.comparator != null) {
                assessment.setComparator(this.comparator);
            }
            
            if (this.comparables != null) {
                assessment.setComparables(this.comparables);
            }
            
            return assessment;
        }
    }

    
    protected class DefaultObligationAssessor implements ObligationAssessor<Obligation> {
        
        @Override
        public boolean accepts(Obligation obligation) {
            // Accepts any obligations
            return true;
        }

        @Override
        public void assess(Obligation obligation, ObligationAssessmentBuilder builder) {
            Set<MetricAssessment> metricAssessments = new HashSet<MetricAssessment>();
            AssessmentResult result = AssessmentResult.SUCCESS;
            
            // Iterate through and assess each metric.
            // Obligation is considered successful if all metrics are successful
            for (Metric metric : obligation.getMetrics()) {
                Log.debug("Assessing metric: {}", metric);
                
                MetricAssessment assessment = builder.assess(metric);
                metricAssessments.add(assessment);
            }
            
            for (MetricAssessment ma : metricAssessments) {
                result = result.max(ma.getResult());
            }
            
            String message = "The obligation requirements were met";
            if (result != AssessmentResult.SUCCESS) {
                message = "At least one metric assessment resulted in the status: " + result;
            }
            
            builder
                .result(result)
                .message(message)
                .obligation(obligation);
        }
        
    }
}
