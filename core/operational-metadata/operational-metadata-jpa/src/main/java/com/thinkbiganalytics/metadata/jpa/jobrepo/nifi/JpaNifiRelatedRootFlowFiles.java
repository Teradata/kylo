package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiRelatedRootFlowFiles;

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
 * Created by sr186054 on 9/10/16.
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
}
