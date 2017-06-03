package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 6/2/17.
 */
public interface NiFiProvenanceEventHoldingBin {

    String getFeedName();

    String getProcessorId();

    String getProcessorName();

    Long getEventId();

    String getFlowFileId();

    DateTime getEventTime();

    String getEventJson();

    boolean isProcessed();
}
