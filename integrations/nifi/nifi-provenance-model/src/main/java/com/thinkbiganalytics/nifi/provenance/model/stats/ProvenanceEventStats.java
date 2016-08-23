package com.thinkbiganalytics.nifi.provenance.model.stats;

/**
 * Created by sr186054 on 8/16/16.
 */
public class ProvenanceEventStats extends BaseStatistics {

    private Long eventId;
    private String processorId;
    private String clusterNodeId;
    private String feedName;


    private String eventDetails;

    private String flowFileId;
    private String rootFlowFileId;
    private String rootProcessGroupId;
    private String processorName;

    public ProvenanceEventStats() {

    }

    public ProvenanceEventStats() {

    }

    public ProvenanceEventStats(String feedName) {
        this.feedName = feedName;
    }


    public String getFeedName() {
        return feedName;
    }

    public String getFlowFileId() {
        return flowFileId;
    }

    public String getRootFlowFileId() {
        return rootFlowFileId;
    }

    public String getRootProcessGroupId() {
        return rootProcessGroupId;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public String getEventDetails() {
        return eventDetails;
    }


    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public void setFlowFileId(String flowFileId) {
        this.flowFileId = flowFileId;
    }

    public void setRootFlowFileId(String rootFlowFileId) {
        this.rootFlowFileId = rootFlowFileId;
    }

    public void setRootProcessGroupId(String rootProcessGroupId) {
        this.rootProcessGroupId = rootProcessGroupId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProvenanceEventStats that = (ProvenanceEventStats) o;

        if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
            return false;
        }
        return !(flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null);

    }

    @Override
    public int hashCode() {
        int result = eventId != null ? eventId.hashCode() : 0;
        result = 31 * result + (flowFileId != null ? flowFileId.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProvenanceEventStats that = (ProvenanceEventStats) o;

        if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
            return false;
        }
        return !(rootFlowFileId != null ? !rootFlowFileId.equals(that.rootFlowFileId) : that.rootFlowFileId != null);

    }

    @Override
    public int hashCode() {
        int result = eventId != null ? eventId.hashCode() : 0;
        result = 31 * result + (rootFlowFileId != null ? rootFlowFileId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventStats{");
        sb.append("eventId=").append(eventId);
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append(", processorsFailed=").append(processorsFailed);
        sb.append('}');
        return sb.toString();
    }


}
