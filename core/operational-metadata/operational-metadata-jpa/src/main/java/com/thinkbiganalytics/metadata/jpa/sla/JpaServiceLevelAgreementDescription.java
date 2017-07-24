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
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "SLA_DESCRIPTION")
public class JpaServiceLevelAgreementDescription implements ServiceLevelAgreementDescription {


    @EmbeddedId
    private ServiceLevelAgreementId slaId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = JpaOpsManagerFeed.class)
    @JoinTable(name = "SLA_FEED", joinColumns = {
        @JoinColumn(name = "SLA_ID", nullable = false, updatable = false) },
               inverseJoinColumns = { @JoinColumn(name = "FEED_ID",
                                                  nullable = false, updatable = false) })
    private Set<OpsManagerFeed> feeds = new HashSet<>(0);

    public JpaServiceLevelAgreementDescription() {

    }

    @Override
    public ServiceLevelAgreementId getSlaId() {
        return slaId;
    }

    public void setSlaId(ServiceLevelAgreementId slaId) {
        this.slaId = slaId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<OpsManagerFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(Set<OpsManagerFeed> feeds) {
        this.feeds = feeds;
    }

    @Embeddable
    public static class ServiceLevelAgreementId extends BaseJpaId implements ServiceLevelAgreement.ID, Serializable {

        private static final long serialVersionUID = 6965221468619613881L;

        @Column(name = "SLA_ID")
        private UUID uuid;

        public ServiceLevelAgreementId() {
        }

        public ServiceLevelAgreementId(Serializable ser) {
            super(ser);
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
