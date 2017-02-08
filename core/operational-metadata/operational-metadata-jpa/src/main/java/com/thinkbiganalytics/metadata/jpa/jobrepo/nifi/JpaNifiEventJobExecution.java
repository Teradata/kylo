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
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Maps the NiFi Provenance Event to the BATCH_JOB_EXECUTION ({@link BatchJobExecution})
 */
@Entity
@Table(name = "BATCH_NIFI_JOB")
public class JpaNifiEventJobExecution implements Serializable, NifiEventJobExecution {

    @OneToOne(targetEntity = JpaBatchJobExecution.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobExecution jobExecution;


    @Column(name = "EVENT_ID")
    private Long eventId;

    @Id
    @Column(name = "FLOW_FILE_ID")
    private String flowFileId;


    public JpaNifiEventJobExecution() {

    }

    public JpaNifiEventJobExecution(Long eventId, String flowFileId) {
        this.flowFileId = flowFileId;
        this.eventId = eventId;
    }

    public JpaNifiEventJobExecution(BatchJobExecution jobExecution, Long eventId, String flowFileId) {
        this.flowFileId = flowFileId;
        this.eventId = eventId;
        this.jobExecution = jobExecution;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Override
    public BatchJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(BatchJobExecution jobExecution) {
        this.jobExecution = jobExecution;
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

        JpaNifiEventJobExecution that = (JpaNifiEventJobExecution) o;

        return !(flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null);

    }

    @Override
    public int hashCode() {
        return flowFileId != null ? flowFileId.hashCode() : 0;
    }

    public static class NifiEventJobExecutionPK implements Serializable {

        private String flowFileId;

        public NifiEventJobExecutionPK() {

        }

        public NifiEventJobExecutionPK(String flowFileId) {
            this.flowFileId = flowFileId;
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

            NifiEventJobExecutionPK that = (NifiEventJobExecutionPK) o;

            return !(flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null);

        }

        @Override
        public int hashCode() {
            return flowFileId != null ? flowFileId.hashCode() : 0;
        }
    }
}
