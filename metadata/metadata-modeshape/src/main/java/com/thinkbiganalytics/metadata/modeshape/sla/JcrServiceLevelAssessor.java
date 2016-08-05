/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.AssessorNotFoundException;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JcrServiceLevelAssessor implements ServiceLevelAssessor {

    private static final Logger log = LoggerFactory.getLogger(JcrServiceLevelAssessor.class);

    private JcrServiceLevelAssessmentProvider assessmentProvider;

    private JcrServiceLevelAgreementProvider agreementProvider;


    private ObligationAssessor<? extends Obligation> defaultObligationAssessor;

    private Set<ObligationAssessor<? extends Obligation>> obligationAssessors;
    private Set<MetricAssessor<? extends Metric, ? extends Serializable>> metricAssessors;


    public JcrServiceLevelAssessor() {
        this.obligationAssessors = Collections.synchronizedSet(new HashSet<ObligationAssessor<? extends Obligation>>());
        this.metricAssessors = Collections.synchronizedSet(new HashSet<MetricAssessor<? extends Metric, ? extends Serializable>>());
        this.defaultObligationAssessor = new DefaultObligationAssessor();
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
    public MetricAssessor<? extends Metric, ? extends Serializable> registerMetricAssessor(MetricAssessor<? extends Metric, ? extends Serializable> assessor) {
        this.metricAssessors.add(assessor);
        return assessor;
    }


    public ServiceLevelAssessment assess(ID id) {

        ServiceLevelAgreement sla = agreementProvider.getAgreement(id);
        if (sla == null) {
            throw new MetadataRepositoryException("Failed to get sla node for " + id);
        }
        return assess(sla);


    }


    public ServiceLevelAssessment assess(ServiceLevelAgreement sla) {
        log.info("Assessing SLA: {}", sla.getName());

        AssessmentResult combinedResult = AssessmentResult.FAILURE;
        JcrServiceLevelAgreement serviceLevelAgreement = (JcrServiceLevelAgreement) sla;
try {
            //create the new Assessment
            JcrServiceLevelAssessment slaAssessment = serviceLevelAgreement.newAssessment();
            List<ObligationGroup> groups = sla.getObligationGroups();

            for (ObligationGroup group : groups) {
                Condition condition = group.getCondition();
                AssessmentResult groupResult = AssessmentResult.SUCCESS;

                for (Obligation ob : group.getObligations()) {
                    ObligationAssessment obAssessment = assess(ob, slaAssessment);
                    // slaAssessment.add(obAssessment);
                    groupResult = groupResult.max(obAssessment.getResult());
                }

                // Short-circuit required or sufficient if necessary.
                switch (condition) {
                    case REQUIRED:
                        if (groupResult == AssessmentResult.FAILURE) {
                            return completeAssessment(slaAssessment, groupResult);
                        }
                        break;
                    case SUFFICIENT:
                        if (groupResult != AssessmentResult.FAILURE) {
                            return completeAssessment(slaAssessment, groupResult);
                        }
                        break;
                    default:
                }

                // Required condition but non-failure, sufficient condition but non-success, or optional condition:
                // continue assessing groups and retain the best of the group results.
                combinedResult = combinedResult.min(groupResult);
            }

            return completeAssessment(slaAssessment, combinedResult);
        }  finally {
            log.debug("Completed assessment of SLA {}: {}", sla.getName(), combinedResult);
        }
    }

    private ObligationAssessment assess(Obligation ob, JcrServiceLevelAssessment serviceLevelAssessment) {
        ObligationAssessmentBuilderImpl builder = new ObligationAssessmentBuilderImpl(ob, serviceLevelAssessment);
        @SuppressWarnings("unchecked")
        ObligationAssessor<Obligation> assessor = (ObligationAssessor<Obligation>) findAssessor(ob);

        log.debug("Assessing obligation \"{}\" with assessor: {}", assessor);

        assessor.assess(ob, builder);

        return builder.build();
    }


    private class ObligationAssessmentBuilderImpl implements ObligationAssessmentBuilder {

        private Obligation obligation;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private Comparator<ObligationAssessment> comparator;
        private List<Comparable<? extends Serializable>> comparables;

        private JcrObligationAssessment assessment;

        public ObligationAssessmentBuilderImpl(Obligation obligation, JcrServiceLevelAssessment serviceLevelAssessment) {
            Node obligationAssessmentNode = JcrUtil.getOrCreateNode(serviceLevelAssessment.getNode(), JcrServiceLevelAssessment.OBLIGATION_ASSESSMENTS, JcrObligationAssessment.NODE_TYPE, true);
            this.obligation = obligation;
            this.assessment = new JcrObligationAssessment(obligationAssessmentNode, this.obligation);
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

            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(value));
            this.comparables.addAll(Arrays.asList(value));
            return this;
        }

        @Override
        public <M extends Metric> MetricAssessment<?> assess(M metric) {
            //NOT SUPPORTED
            return null;
        }


        public <M extends Metric> MetricAssessment<?> assess(Node metricNode) {
            M metric = JcrUtil.getGenericJson(metricNode, JcrPropertyConstants.JSON);
            MetricAssessor<M, ?> assessor = findAssessor(metric);
            MetricAssessmentBuilderImpl builder = new MetricAssessmentBuilderImpl(metric, metricNode, this.assessment);

            assessor.assess(metric, builder);
            MetricAssessment<?> metricAssmt = builder.build();
            // this.assessment.add(metricAssmt);
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

    private class MetricAssessmentBuilderImpl<D extends Serializable> implements MetricAssessmentBuilder<D> {

        private Metric metric;
        private Node metricNode;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private D data;
        private Comparator<MetricAssessment<D>> comparator;
        private List<Comparable<? extends Serializable>> comparables;

        private JcrObligationAssessment obligationAssessment;


        public MetricAssessmentBuilderImpl(Metric metric, Node metricNode, JcrObligationAssessment obligationAssessment) {
            this.obligationAssessment = obligationAssessment;
            this.metric = metric;
            this.metricNode = metricNode;
        }


        /**
         * not used
         **/
        public MetricAssessmentBuilderImpl(Metric metric) {
            this.metric = metric;
        }

        /**
         * not used
         **/
        @Override
        public MetricAssessmentBuilder<D> metric(Metric metric) {
            this.metric = metric;
            return this;
        }


        @Override
        public MetricAssessmentBuilder<D> message(String descr) {
            this.message = descr;
            return this;
        }

        @Override
        public MetricAssessmentBuilder<D> result(AssessmentResult result) {
            this.result = result;
            return this;
        }

        @Override
        public MetricAssessmentBuilder<D> data(D data) {
            this.data = data;
            return this;
        }

        @Override
        public MetricAssessmentBuilder<D> comparitor(Comparator<MetricAssessment<D>> comp) {
            this.comparator = comp;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetricAssessmentBuilder<D> compareWith(Comparable<? extends Serializable> value,
                                                      Comparable<? extends Serializable>... otherValues) {
            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(value));
            this.comparables.addAll(Arrays.asList(value));
            return this;
        }

        protected MetricAssessment build() {

            Node metricAssessmentNode = JcrUtil.getOrCreateNode(obligationAssessment.getNode(), JcrObligationAssessment.METRIC_ASSESSMENTS, JcrMetricAssessment.NODE_TYPE, true);

            JcrMetricAssessment<D> assessment = new JcrMetricAssessment<D>(metricAssessmentNode, this.metricNode);
            assessment.setMessage(this.message);
            assessment.setResult(this.result);
            assessment.setData(this.data);

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
            ObligationAssessmentBuilderImpl obligationAssessmentBuilder = (ObligationAssessmentBuilderImpl) builder;
            AssessmentResult result = AssessmentResult.SUCCESS;
            try {

                JcrObligation jcrObligation = (JcrObligation) obligation;
                Iterator<Node> itr = (Iterator<Node>) jcrObligation.getNode().getNodes(JcrObligation.METRICS);
                while (itr.hasNext()) {
                    Node metricNode = itr.next();
                    MetricAssessment assessment = obligationAssessmentBuilder.assess(metricNode);
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
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to assess Obligation ", e);
            }
        }

    }

    private ServiceLevelAssessment completeAssessment(JcrServiceLevelAssessment slaAssessment, AssessmentResult result) {
        slaAssessment.setResult(result);
        if (result == AssessmentResult.SUCCESS) {
            slaAssessment.setMessage("SLA assessment requirements were met");
        } else {
            slaAssessment.setMessage("At least one of the SLA obligations resulted in the status: " + result);
        }

        return slaAssessment;
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
    protected <M extends Metric> MetricAssessor<M, ?> findAssessor(M metric) {
        synchronized (this.metricAssessors) {
            for (MetricAssessor<? extends Metric, ? extends Serializable> accessor : this.metricAssessors) {
                if (accessor.accepts(metric)) {
                    return (MetricAssessor<M, ?>) accessor;
                }
            }
        }
        throw new AssessorNotFoundException(metric);
    }


}
