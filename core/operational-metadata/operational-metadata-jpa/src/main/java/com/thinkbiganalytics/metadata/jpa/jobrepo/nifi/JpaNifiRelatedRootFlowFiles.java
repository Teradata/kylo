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

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiRelatedRootFlowFiles;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity to store any Feed Flow files that are related to each other.
 * Job executions can become related if the started off as individual jobs but later get merged together to a single flow file
 *
 * @see com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionProvider#checkAndRelateJobs(ProvenanceEventRecordDTO, NifiEvent)
 */
@Entity
@Table(name = "NIFI_RELATED_ROOT_FLOW_FILES")
public class JpaNifiRelatedRootFlowFiles implements NifiRelatedRootFlowFiles {

    @EmbeddedId
    private NifiRelatedFlowFilesPK relatedFlowFilesPK;


    @ManyToOne(targetEntity = JpaNifiEventJobExecution.class, optional = true)
    @JoinColumn(name = "FLOW_FILE_ID", referencedColumnName = "FLOW_FILE_ID", insertable = false, updatable = false)
    private JpaNifiEventJobExecution eventJobExecution;


    @ManyToOne(targetEntity = JpaNifiEvent.class, optional = true)
    @JoinColumns({
                     @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID"),
                     @JoinColumn(name = "EVENT_FLOW_FILE_ID", referencedColumnName = "FLOW_FILE_ID"),
                 })
    private NifiEvent event;

    @Column(name = "FLOW_FILE_ID", insertable = false, updatable = false)
    private String flowFileId;

    @Column(name = "RELATION_ID", insertable = false, updatable = false)
    private String relationId;

    public JpaNifiRelatedRootFlowFiles() {

    }


    public JpaNifiRelatedRootFlowFiles(NifiEvent event, String rootFlowFile, String relationId) {
        this.relatedFlowFilesPK = new NifiRelatedFlowFilesPK(rootFlowFile, relationId);
        this.event = event;
    }

    public NifiRelatedFlowFilesPK getRelatedFlowFilesPK() {
        return relatedFlowFilesPK;
    }

    @Override
    public JpaNifiEventJobExecution getEventJobExecution() {
        return eventJobExecution;
    }

    @Override
    public NifiEvent getEvent() {
        return event;
    }

    public void setEvent(NifiEvent event) {
        this.event = event;
    }

    @Embeddable
    public static class NifiRelatedFlowFilesPK implements Serializable {

        @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        @Column(name = "RELATION_ID")
        private String relationId;

        public NifiRelatedFlowFilesPK() {

        }

        public NifiRelatedFlowFilesPK(String flowFileId, String relationId) {
            this.flowFileId = flowFileId;
            this.relationId = relationId;
        }

        public String getFlowFileId() {
            return flowFileId;
        }

        public void setFlowFileId(String flowFileId) {
            this.flowFileId = flowFileId;
        }

        public String getRelationId() {
            return relationId;
        }

        public void setRelationId(String relationId) {
            this.relationId = relationId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NifiRelatedFlowFilesPK that = (NifiRelatedFlowFilesPK) o;

            if (!flowFileId.equals(that.flowFileId)) {
                return false;
            }
            return relationId.equals(that.relationId);

        }

        @Override
        public int hashCode() {
            int result = flowFileId.hashCode();
            result = 31 * result + relationId.hashCode();
            return result;
        }
    }
}
