package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/17/16.
 */
public class StatsModel {

    public static ProvenanceEventStats toProvenanceEventStats(String feedName, ProvenanceEventRecordDTO event) {
        ProvenanceEventStats stats = new ProvenanceEventStats(feedName);
        stats.setEventId(event.getEventId());
        stats.setProcessorId(event.getComponentId());
        stats.setClusterNodeId(event.getClusterNodeId());
        stats.setTime(new DateTime(event.getEventTime()));
        stats.setDuration(event.getEventDuration() != null ? event.getEventDuration() : 0L);
        stats.setBytesIn(event.getInputContentClaimFileSizeBytes() != null ? event.getInputContentClaimFileSizeBytes() : 0L);
        stats.setBytesOut(event.getOutputContentClaimFileSizeBytes() != null ? event.getOutputContentClaimFileSizeBytes() : 0L);
        stats.setFlowFileId(event.getFlowFileUuid());
        stats.setRootFlowFileId(event.getFlowFile().getRootFlowFile().getId());
        stats.setEventDetails(event.getDetails());
        stats.setRootProcessGroupId(event.getFlowFile().getRootFlowFile().getFirstEvent().getGroupId());
        stats.setJobsStarted((event.getFlowFile().isRootFlowFile() && event.getFlowFile().getRootFlowFile().isStartOfCurrentFlowFile(event)) ? 1L : 0L);
        stats.setJobsFinished(event.getFlowFile().isFlowComplete() ? 1L : 0L);
        stats.setFlowFilesStarted(event.isStartOfCurrentFlowFile() ? 1L : 0L);
        stats.setFlowFilesFinished(event.getFlowFile().isCurrentFlowFileComplete() ? 1L : 0L);

        //calc Job duration????
        if (stats.getJobsFinished() == 1L) {
            //  Long totalJobTime = event.getEventTime().getTime() - event.getFlowFile().getFirstEvent().getEventTime().getTime();

        }

        //check if it is ia faliure processor
        // TODO!!! is there a better way???  cant we just examine the  event.getRelationship() to see if it contains "failure" ???
        //  NifiFlowProcessor processor = NifiFlowCache.instance().getProcessor(event.getFlowFile().getFirstEvent().getGroupId(), event.getComponentId());
        //  if (processor.isFailure()) {
        //      this.processorsFailed = 1L;
        //  }
        return stats;
    }


}
