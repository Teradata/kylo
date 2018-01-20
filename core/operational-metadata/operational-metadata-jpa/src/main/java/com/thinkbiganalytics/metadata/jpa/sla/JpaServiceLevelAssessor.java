/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.MetadataAccess;
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
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * A service level agreement assessor that will assess service level agreements checking the obligations and metrics and validating if the agreement is violated or not
 */
public class JpaServiceLevelAssessor implements ServiceLevelAssessor {

    private static final Logger log = LoggerFactory.getLogger(JpaServiceLevelAssessor.class);

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private JpaServiceLevelAssessmentProvider assessmentProvider;

    @Inject
    private ServiceLevelAgreementProvider agreementProvider;


    private ObligationAssessor<? extends Obligation> defaultObligationAssessor;

    private Set<ObligationAssessor<? extends Obligation>> obligationAssessors;
    private Set<MetricAssessor<? extends Metric, ? extends Serializable>> metricAssessors;


    public JpaServiceLevelAssessor() {
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


    private List<Comparable<? extends Serializable>> sanitizeComparablesArray(Comparable<? extends Serializable>... comparables) {
        List<Comparable<? extends Serializable>> list = new ArrayList<>();
        if (comparables != null) {
            for (Comparable<? extends Serializable> comparable : comparables) {
                list.add(sanitizeComparable(comparable));
            }
        }
        return list;
    }


    private Comparable<? extends Serializable> sanitizeComparable(Comparable<? extends Serializable> comparable) {
        if (comparable != null && (comparable instanceof Date)) {
            return ((Date) comparable).getTime();
        } else {
            return comparable;
        }
    }


    /**
     * Needs to be wrapped in metadataAccess.read
     */
    public ServiceLevelAssessment assess(ID id) {

        ServiceLevelAgreement sla = agreementProvider.getAgreement(id);
        if (sla == null) {
            //TODO add explicit NotFoundException
            throw new RuntimeException("Failed to get sla node for " + id);
        }
        return assess(sla);


    }

    @Override
    public ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement sla) {
        return this.metadataAccess.read(() -> assessmentProvider.findLatestAssessment(sla.getId()),
                                        MetadataAccess.SERVICE);
    }

    /**
     * Assess the SLA (coming from JCR)
     *
     * @param sla the SLA to be assessed
     */
    public ServiceLevelAssessment assess(ServiceLevelAgreement sla) {

        log.info("Assessing SLA: {}", sla.getName());

        ServiceLevelAssessment assessment = null;

        ServiceLevelAgreement serviceLevelAgreement = sla;
        assessment = this.metadataAccess.commit(() -> {
            AssessmentResult combinedResult = AssessmentResult.SUCCESS;
            try {

                //create the new Assessment
                JpaServiceLevelAssessment slaAssessment = new JpaServiceLevelAssessment();
                slaAssessment.setId(JpaServiceLevelAssessment.SlaAssessmentId.create());
                slaAssessment.setAgreement(serviceLevelAgreement);
                List<ObligationGroup> groups = sla.getObligationGroups();

                for (ObligationGroup group : groups) {
                    Condition condition = group.getCondition();
                    AssessmentResult groupResult = AssessmentResult.SUCCESS;
                    Set<ObligationAssessment> obligationAssessments = new HashSet<>();
                    log.debug("Assessing obligation group {} with {} obligations", group, group.getObligations().size());
                    for (Obligation ob : group.getObligations()) {
                        ObligationAssessment obAssessment = assess(ob, slaAssessment);
                        obligationAssessments.add(obAssessment);
                        // slaAssessment.add(obAssessment);
                        groupResult = groupResult.max(obAssessment.getResult());
                    }
                    slaAssessment.setObligationAssessments(obligationAssessments);

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
                    combinedResult = combinedResult.max(groupResult);
                }

                return completeAssessment(slaAssessment, combinedResult);

            } finally {
                log.debug("Completed assessment of SLA {}: {}", sla.getName(), combinedResult);
            }
        }, MetadataAccess.SERVICE);
        return assessment;
    }

    private ObligationAssessment assess(Obligation ob, JpaServiceLevelAssessment serviceLevelAssessment) {
        ObligationAssessmentBuilderImpl builder = new ObligationAssessmentBuilderImpl(ob, serviceLevelAssessment);
        @SuppressWarnings("unchecked")
        ObligationAssessor<Obligation> assessor = (ObligationAssessor<Obligation>) findAssessor(ob);

        log.debug("Assessing obligation \"{}\" with assessor: {}", assessor);

        assessor.assess(ob, builder);

        return builder.build();
    }

    private ServiceLevelAssessment completeAssessment(JpaServiceLevelAssessment slaAssessment, AssessmentResult result) {
        slaAssessment.setResult(result);
        String slaName = slaAssessment.getAgreement() != null ? slaAssessment.getAgreement().getName() : "";
        if (result == AssessmentResult.SUCCESS) {

            slaAssessment.setMessage("SLA assessment requirements were met for '" + slaName + "'");
        } else {
            slaAssessment.setMessage("At least one of the SLA obligations for '" + slaName + "' resulted in the status: " + result);
        }

        //save it
        assessmentProvider.save(slaAssessment);
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

    private class ObligationAssessmentBuilderImpl implements ObligationAssessmentBuilder {

        private Obligation obligation;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private Comparator<ObligationAssessment> comparator;
        private List<Comparable<? extends Serializable>> comparables;
        private ServiceLevelAssessment serviceLevelAssessment;

        private JpaObligationAssessment assessment;

        public ObligationAssessmentBuilderImpl(Obligation obligation, JpaServiceLevelAssessment serviceLevelAssessment) {

            this.obligation = obligation;
            this.assessment = new JpaObligationAssessment();
            this.assessment.setObligation(obligation);
            this.serviceLevelAssessment = serviceLevelAssessment;
            serviceLevelAssessment.getObligationAssessments().add(this.assessment);
            this.assessment.setServiceLevelAssessment(serviceLevelAssessment);
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
                                                       final Comparable<? extends Serializable>... otherValues) {

            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(sanitizeComparable(value)));
            this.comparables.addAll(sanitizeComparablesArray(otherValues));
            return this;
        }

        @Override
        public <M extends Metric> MetricAssessment<?> assess(M metric) {
            MetricAssessor<M, ?> assessor = findAssessor(metric);
            MetricAssessmentBuilderImpl builder = new MetricAssessmentBuilderImpl(metric, this.assessment);

            assessor.assess(metric, builder);
            MetricAssessment<?> metricAssmt = builder.build();
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
            this.serviceLevelAssessment.getObligationAssessments().add(this.assessment);

            return this.assessment;
        }
    }

    private class MetricAssessmentBuilderImpl<D extends Serializable> implements MetricAssessmentBuilder<D> {

        private Metric metric;
        private String message = "";
        private AssessmentResult result = AssessmentResult.SUCCESS;
        private D data;
        private Comparator<MetricAssessment<D>> comparator;
        private List<Comparable<? extends Serializable>> comparables;

        private JpaObligationAssessment obligationAssessment;


        public MetricAssessmentBuilderImpl(Metric metric, JpaObligationAssessment obligationAssessment) {
            this.obligationAssessment = obligationAssessment;
            this.metric = metric;

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
            this.comparables = new ArrayList<Comparable<? extends Serializable>>(Arrays.asList(sanitizeComparable(value)));
            this.comparables.addAll(sanitizeComparablesArray(otherValues));
            return this;
        }


        protected MetricAssessment build() {
            JpaMetricAssessment<D> assessment = new JpaMetricAssessment<>();
            assessment.setMetric(this.metric);
            assessment.setMessage(this.message);
            assessment.setResult(this.result);
            assessment.setMetricDescription(this.metric.getDescription());
            //assessment.setData(this.data);

            if (this.comparator != null) {
                assessment.setComparator(this.comparator);
            }

            if (this.comparables != null) {
                assessment.setComparables(this.comparables);
            }
            obligationAssessment.getMetricAssessments().add(assessment);
            assessment.setObligationAssessment(obligationAssessment);
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
            log.debug("Assessing obligation '{}' with {} metrics", obligation, obligation.getMetrics().size());

            Set<MetricAssessment> metricAssessments = new HashSet<MetricAssessment>();
            ObligationAssessmentBuilderImpl obligationAssessmentBuilder = (ObligationAssessmentBuilderImpl) builder;
            AssessmentResult result = AssessmentResult.SUCCESS;

            for (Metric metric : obligation.getMetrics()) {
                log.debug("Assessing obligation metric '{}'", metric);
                MetricAssessment assessment = obligationAssessmentBuilder.assess(metric);
                metricAssessments.add(assessment);
            }

            for (MetricAssessment ma : metricAssessments) {
                result = result.max(ma.getResult());
            }

            String message = "The obligation requirements were met";
            if (result != AssessmentResult.SUCCESS) {
                message = "At least one metric assessment resulted in the status: " + result;
            }

            log.debug("Assessment outcome '{}'", message);

            builder
                .result(result)
                .message(message)
                .obligation(obligation);

        }

    }


}
