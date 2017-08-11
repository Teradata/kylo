/**
 *
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
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
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class SimpleServiceLevelAssessment implements ServiceLevelAssessment {

    private static final long serialVersionUID = 2752726049105871947L;

    private DateTime time;
    private ServiceLevelAgreement sla;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private Set<ObligationAssessment> obligationAssessments;
    private ServiceLevelAssessment.ID id;




    /**
     *
     */
    protected SimpleServiceLevelAssessment() {
        this.id = new AssessmentId(UUID.randomUUID());
        this.time = DateTime.now();
        this.obligationAssessments = new HashSet<ObligationAssessment>();
    }

    public SimpleServiceLevelAssessment(ServiceLevelAgreement sla) {
        this();
        this.sla = sla;
    }

    public SimpleServiceLevelAssessment(ServiceLevelAgreement sla, String message, AssessmentResult result) {
        super();
        this.id = new AssessmentId(UUID.randomUUID());
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

    protected void setTime(DateTime time) {
        this.time = time;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getSLA()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        return this.sla;
    }

    @Override
    public ServiceLevelAgreement.ID getServiceLevelAgreementId() {
        return this.sla != null ? this.sla.getId() : null;
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getMessage()
         */
    @Override
    public String getMessage() {
        return this.message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment#getResult()
     */
    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
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

    protected boolean add(ObligationAssessment assessment) {
        return this.obligationAssessments.add(assessment);
    }

    protected boolean addAll(Collection<? extends ObligationAssessment> assessments) {
        return this.obligationAssessments.addAll(assessments);
    }

    protected void setSla(ServiceLevelAgreement sla) {
        this.sla = sla;
    }

    @Override
    public ServiceLevelAgreementDescription getServiceLevelAgreementDescription() {
        return new SimpleServiceLevelAgreementDescription(this.getAgreement().getId(),this.getAgreement().getName(),this.getAgreement().getDescription());
    }

    @Override
    public ID getId() {
        return this.id;
    }

    public static class AssessmentId implements ID {

        private static final long serialVersionUID = -9084653006891727475L;

        private String idValue;


        public AssessmentId() {
        }

        public AssessmentId(Serializable ser) {
            if (ser instanceof String) {
                String uuid = (String) ser;
                if (!StringUtils.contains(uuid, "-")) {
                    uuid = ((String) ser).replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
                }
                setUuid(UUID.fromString(uuid));

            } else if (ser instanceof UUID) {
                setUuid((UUID) ser);
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }

        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }

        public UUID getUuid() {
            return UUID.fromString(idValue);
        }

        public void setUuid(UUID uuid) {
            this.idValue = uuid.toString();

        }
    }

    private static class SimpleServiceLevelAgreementDescription implements ServiceLevelAgreementDescription {

        private String description;
        private ServiceLevelAgreement.ID slaId;
        private String name;

        public SimpleServiceLevelAgreementDescription() {
        }

        public SimpleServiceLevelAgreementDescription(ServiceLevelAgreement.ID slaId, String name,String description) {
            this.description = description;
            this.slaId = slaId;
            this.name = name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public ServiceLevelAgreement.ID getSlaId() {
            return slaId;
        }

        public void setSlaId(ServiceLevelAgreement.ID slaId) {
            this.slaId = slaId;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
