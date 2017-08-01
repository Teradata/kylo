package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 7/29/17.
 */
public interface NifiFeedProcessorErrors {

    /**
     * return the processor id
     */
    String getProcessorId();

    /**
     * set the processor id
     */
    void setProcessorId(String processorId);

    /**
     * Return  the unique id  for this feed processor statistic entry
     *
     * @return the unique id  for this feed processor statistic entry
     */
    String getId();

    /**
     * set the unique id for this entry
     */
    void setId(String id);


    /**
     * Return the processor name
     *
     * @return the processor name
     */
    String getProcessorName();

    /**
     * set the processor name
     */
    void setProcessorName(String processorName);

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeedName();

    /**
     * set the feed name
     */
    void setFeedName(String feedName);


    /**
     * Get the latest flow file id for this group and collection interval
     *
     * @return the latest flow file id for this group and collection interval
     */
    String getLatestFlowFileId();

    /**
     * Set the latest flowfile id for this group and collection interval
     *
     * @param flowFileId the flow file id
     */
    void setLatestFlowFileId(String flowFileId);

    /**
     * @return the number of events that failed
     */
    Long getFailedCount();

    /**
     * set the total events that are failure events
     */
    void setFailedCount(Long failedCount);

    String getErrorMessages();

    DateTime getErrorMessageTimestamp();

}
