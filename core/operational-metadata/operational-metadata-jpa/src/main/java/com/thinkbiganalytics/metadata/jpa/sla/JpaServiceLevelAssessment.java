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
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Entity representing service level assessment results for Service Level Agreement (SLA).
 * SLA's are defined in Modeshape, but their assessments are stored here
 * Service level assessments contain 1 ore more attached {@link JpaObligationAssessment}.
 * Each Obligation assessment contains 1 or more {@link JpaMetricAssessment}
 * the result of this service level assessment come from the results of the {@link JpaObligationAssessment}'s
 */
@Entity
@Table(name = "SLA_ASSESSMENT")
public class JpaServiceLevelAssessment extends AbstractAuditedEntity implements ServiceLevelAssessment {


    @EmbeddedId
    private SlaAssessmentId id;

    @Transient
    private ServiceLevelAgreement agreement;


    @ManyToOne(targetEntity = JpaServiceLevelAgreementDescription.class, optional = true)
    @JoinColumn(name = "SLA_ID", insertable = false,updatable = false)
    private ServiceLevelAgreementDescription serviceLevelAgreementDescription;

    @Column(name = "SLA_ID")
    private ServiceLevelAgreementDescriptionId slaId;

    @Column(name = "MESSAGE")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT")
    private AssessmentResult result;

    @OneToMany(targetEntity = JpaObligationAssessment.class, mappedBy = "serviceLevelAssessment", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ObligationAssessment> obligationAssessments = new HashSet<>();

    public JpaServiceLevelAssessment() {

    }

    public SlaAssessmentId getId() {
        return id;
    }

    public void setId(SlaAssessmentId id) {
        this.id = id;
    }


    @Override
    public ServiceLevelAgreement.ID getServiceLevelAgreementId() {
        return slaId;
    }

    public ServiceLevelAgreement getAgreement() {
        return agreement;
    }

    public void setAgreement(ServiceLevelAgreement agreement) {
        this.agreement = agreement;
        this.setSlaId(new ServiceLevelAgreementDescriptionId(agreement.getId().toString()));
    }

    public ServiceLevelAgreement.ID getSlaId() {
        return slaId;
    }

    public void setSlaId(ServiceLevelAgreementDescriptionId slaId) {
        this.slaId = slaId;
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

    @Override
    public DateTime getTime() {
        return super.getCreatedTime();
    }

    @Override
    public Set<ObligationAssessment> getObligationAssessments() {
        return obligationAssessments;
    }

    public void setObligationAssessments(Set<ObligationAssessment> obligationAssessments) {
        this.obligationAssessments = obligationAssessments;
    }

    public ServiceLevelAgreementDescription getServiceLevelAgreementDescription() {
        return serviceLevelAgreementDescription;
    }

    public void setServiceLevelAgreementDescription(ServiceLevelAgreementDescription serviceLevelAgreementDescription) {
        this.serviceLevelAgreementDescription = serviceLevelAgreementDescription;
    }

    @Override
    public int compareTo(ServiceLevelAssessment sla) {

        ComparisonChain chain = ComparisonChain
            .start()
            .compare(this.getResult(), sla.getResult())
            .compare(this.getAgreement().getName(), sla.getAgreement().getName());

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

    @Embeddable
    public static class SlaAssessmentId extends BaseJpaId implements ServiceLevelAssessment.ID, Serializable {

        private static final long serialVersionUID = 6965221468619613881L;

        @Column(name = "id")
        private UUID uuid;

        public SlaAssessmentId() {
        }

        public SlaAssessmentId(Serializable ser) {
            super(ser);
        }

        public static SlaAssessmentId create() {
            return new SlaAssessmentId(UUID.randomUUID());
        }

        @Override
        public UUID getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

}
