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
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplate;
import com.thinkbiganalytics.metadata.jpa.common.JpaVelocityTemplate;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by sr186054 on 10/5/17.
 */
@Entity
@Table(name = "SLA_ACTION_TEMPLATE")
public class JpaServiceLevelAgreementActionTemplate implements ServiceLevelAgreementActionTemplate {

    @EmbeddedId
    private ServiceLevelAgreementActionTemplateId id;

    @ManyToOne(targetEntity = JpaServiceLevelAgreementDescription.class, fetch= FetchType.LAZY)
    @JoinColumn(name="SLA_DESCRIPTION_ID", nullable = false)
    private JpaServiceLevelAgreementDescription serviceLevelAgreementDescription;

    @ManyToOne(targetEntity = JpaVelocityTemplate.class, fetch= FetchType.LAZY)
    @JoinColumn(name = "VELOCITY_TEMPLATE_ID", nullable = false)
    private JpaVelocityTemplate velocityTemplate;

    @Override
    public ID getId() {
        return id;
    }

    public void setId(ServiceLevelAgreementActionTemplateId id) {
        this.id = id;
    }


    public VelocityTemplate getVelocityTemplate() {
        return velocityTemplate;
    }

    public void setVelocityTemplate(JpaVelocityTemplate velocityTemplate) {
        this.velocityTemplate = velocityTemplate;
    }

    public JpaServiceLevelAgreementDescription getServiceLevelAgreementDescription() {
        return serviceLevelAgreementDescription;
    }

    public void setServiceLevelAgreementDescription(JpaServiceLevelAgreementDescription serviceLevelAgreementDescription) {
        this.serviceLevelAgreementDescription = serviceLevelAgreementDescription;
    }

    @Embeddable
    public static class ServiceLevelAgreementActionTemplateId extends BaseJpaId implements ID {


        private static final long serialVersionUID = -5255357654415019054L;

        @Column(name = "id")
        private UUID uuid;

        public ServiceLevelAgreementActionTemplateId() {
        }

        public ServiceLevelAgreementActionTemplateId(Serializable ser) {
            super(ser);
        }

        public static ServiceLevelAgreementActionTemplateId create() {
            return new ServiceLevelAgreementActionTemplateId(UUID.randomUUID());
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
