package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.JpaBatchStepExecution;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Maps a Provenance Event {@link com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent} to a Step Execution {@link BatchStepExecution}
 */
@Entity
@Table(name = "BATCH_NIFI_STEP")
public class JpaNifiEventStepExecution implements Serializable, NifiEventStepExecution {

    @EmbeddedId
    private NifiEventStepExecutionPK eventStepExecutionPK;

    @OneToOne(targetEntity = JpaBatchJobExecution.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobExecution jobExecution;

    @OneToOne(targetEntity = JpaBatchStepExecution.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private BatchStepExecution stepExecution;


    @Column(name = "EVENT_ID", insertable = false, updatable = false)
    private Long eventId;

    @Column(name = "COMPONENT_ID")
    private String componentId;

    @Column(name = "JOB_FLOW_FILE_ID")
    private String jobFlowFileId;

    @Column(name = "FLOW_FILE_ID", insertable = false, updatable = false)
    private String flowFileId;


    public JpaNifiEventStepExecution() {

    }

    public JpaNifiEventStepExecution(Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
    }

    public JpaNifiEventStepExecution(BatchStepExecution stepExecution, Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
        this.stepExecution = stepExecution;
        this.jobExecution = stepExecution.getJobExecution();
    }

    public JpaNifiEventStepExecution(BatchJobExecution jobExecution, BatchStepExecution stepExecution, Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
        this.jobExecution = jobExecution;
        this.stepExecution = stepExecution;
    }

    @Override
    public Long getEventId() {
        return eventId;
    }

    @Override
    public String getFlowFileId() {
        return flowFileId;
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    @Override
    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }

    @Embeddable
    public static class NifiEventStepExecutionPK implements Serializable {

        @Column(name = "EVENT_ID")
        private Long eventId;

        @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        public NifiEventStepExecutionPK() {

        }

        public NifiEventStepExecutionPK(Long eventId, String flowFileId) {
            this.eventId = eventId;
            this.flowFileId = flowFileId;
        }

        public Long getEventId() {
            return eventId;
        }

        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }

        public String getFlowFileId() {
            return flowFileId;
        }

        public void setFlowFileId(String flowFileId) {
            this.flowFileId = flowFileId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NifiEventStepExecutionPK that = (NifiEventStepExecutionPK) o;

            if (!eventId.equals(that.eventId)) {
                return false;
            }
            return flowFileId.equals(that.flowFileId);

        }

        @Override
        public int hashCode() {
            int result = eventId.hashCode();
            result = 31 * result + flowFileId.hashCode();
            return result;
        }
    }


}
