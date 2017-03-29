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

import com.google.common.collect.ComparisonChain;
import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity representing obligation assessment results for Service Level Agreement (SLA).
 * SLA's are defined in Modeshape, but their assessments are stored here
 * Obligation assessments are attached to the overall {@link JpaServiceLevelAssessment}.
 * Each Obligation assessment contains 1 or more {@link JpaMetricAssessment}
 * the result of this Obligation assessment comes from the results of the  {@link JpaMetricAssessment}'s
 */
@Entity
@Table(name = "SLA_OBLIGATION_ASSESSMENT")
public class JpaObligationAssessment extends AbstractAuditedEntity implements ObligationAssessment {

    @Transient
    public static final Comparator<ObligationAssessment> DEF_COMPARATOR = new DefaultComparator();
    @Transient
    private Comparator<ObligationAssessment> comparator = DEF_COMPARATOR;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Transient
    private Obligation obligation;

    @Column(name = "OBLIGATION_ID")
    private String obligationId;

    @Column(name = "MESSAGE")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT")
    private AssessmentResult result;

    @ManyToOne(targetEntity = JpaServiceLevelAssessment.class)
    @JoinColumn(name = "SLA_ASSESSMENT_ID")
    private ServiceLevelAssessment serviceLevelAssessment;


    @OneToMany(targetEntity = JpaMetricAssessment.class, mappedBy = "obligationAssessment", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MetricAssessment> metricAssessments = new HashSet<>();

    @Convert(converter = ComparablesAttributeConverter.class)
    @Column(name = "COMPARABLES")
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();


    public JpaObligationAssessment() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Obligation getObligation() {
        return obligation;
    }

    public void setObligation(Obligation obligation) {
        this.obligation = obligation;
        this.obligationId = obligation.getId() != null ? obligation.getId().toString() : null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AssessmentResult getResult() {
        return result;
    }

    public void setResult(AssessmentResult result) {
        this.result = result;
    }

    public String getObligationId() {
        return obligationId;
    }

    public void setObligationId(String obligationId) {
        this.obligationId = obligationId;
    }

    @Override
    public Set<MetricAssessment> getMetricAssessments() {
        return metricAssessments;
    }

    protected void setComparator(Comparator<ObligationAssessment> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }

    @Override
    public int compareTo(ObligationAssessment obAssessment) {
        return this.comparator.compare(this, obAssessment);
    }

    public void addMetricAssessment(MetricAssessment metricAssessment) {
        getMetricAssessments().add(metricAssessment);
    }

    public ServiceLevelAssessment getServiceLevelAssessment() {
        return serviceLevelAssessment;
    }

    public void setServiceLevelAssessment(ServiceLevelAssessment serviceLevelAssessment) {
        this.serviceLevelAssessment = serviceLevelAssessment;
    }

    public static class ComparablesAttributeConverter extends JsonAttributeConverter<List<Comparable<? extends Serializable>>> {

    }

    protected static class DefaultComparator implements Comparator<ObligationAssessment> {

        @Override
        public int compare(ObligationAssessment o1, ObligationAssessment o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof JpaObligationAssessment && o2 instanceof JpaObligationAssessment) {
                JpaObligationAssessment s1 = (JpaObligationAssessment) o1;
                JpaObligationAssessment s2 = (JpaObligationAssessment) o2;

                for (int idx = 0; idx < s1.comparables.size(); idx++) {
                    chain = chain.compare(s1.comparables.get(idx), s2.comparables.get(idx));
                }
            }

            List<MetricAssessment<Serializable>> list1 = new ArrayList<>(o1.getMetricAssessments());
            List<MetricAssessment<Serializable>> list2 = new ArrayList<>(o2.getMetricAssessments());

            chain = chain.compare(list1.size(), list2.size());
            if (chain.result() != 0) {
                return chain.result();
            }
            Collections.sort(list1);
            Collections.sort(list2);

            for (int idx = 0; idx < list1.size(); idx++) {
                chain = chain.compare(list1.get(idx), list2.get(idx));
            }

            return chain.result();
        }

    }
}
