package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Represents high level flow and job statistics for a given feed and processor
 */
public interface NifiFeedProcessorStats {

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
     * Return the feed process group id
     *
     * @return the feed process group id
     */
    String getFeedProcessGroupId();

    /**
     * Set the feed process group id
     */
    void setFeedProcessGroupId(String feedProcessGroupId);

    /**
     * Return a unique id representing the collection of statistics captured with this batch of data
     *
     * @return a unique id representing the collection of statistics captured with this batch of data
     */
    String getCollectionId();

    /**
     * set the collection id
     */
    void setCollectionId(String collectionId);

    /**
     * Return the sum of the duration in millis of events for this feed/processor during its collection
     *
     * @return the sum of the duration in millis of events for this feed/processor during its collection
     */
    Long getDuration();

    /**
     * set the duration in millis
     */
    void setDuration(Long duration);

    /**
     * return the total bytes taken in by this feed and processor
     *
     * @return the total bytes taken in by this feed and processor
     */
    Long getBytesIn();

    /**
     * set the total bytes taken in by this processor
     */
    void setBytesIn(Long bytesIn);

    /**
     * return the total bytes out by this feed and processor
     *
     * @return the total bytes out by this feed and processor
     */
    Long getBytesOut();

    /**
     * set the total bytes out
     */
    void setBytesOut(Long bytesOut);

    /**
     * return the total events for this feed and processor
     *
     * @return the total events for  this feed and processor
     */
    Long getTotalCount();

    /**
     * set the total event count
     */
    void setTotalCount(Long totalCount);


    /**
     * @return the number of events that failed
     */
    Long getFailedCount();

    /**
     * set the total events that are failure events
     */
    void setFailedCount(Long failedCount);


    /**
     * return the total jobs started by this feed and processor
     *
     * @return the total jobs started by this feed and processor
     */
    Long getJobsStarted();

    /**
     * set the jobs started
     */
    void setJobsStarted(Long jobsStarted);

    /**
     * return the total jobs finished by this feed and processor
     *
     * @return the total jobs finished by this feed and processor
     */
    Long getJobsFinished();

    /**
     * set the jobs finished
     */
    void setJobsFinished(Long jobsFinished);

    /**
     * return the total jobs failed by this feed and processor
     *
     * @return the total jobs failed by this feed and processor
     */
    Long getJobsFailed();

    /**
     * set the jobs failed
     */
    void setJobsFailed(Long jobsFailed);

    /**
     * return the total job duration in millis for this feed and processor
     *
     * @return the total job duration in millis for this feed and processor
     */
    Long getJobDuration();

    /**
     * set the job duration in millis
     */
    void setJobDuration(Long jobDuration);

    /**
     * return the successful job duration in millis for this feed and processor
     *
     * @return the total ob duration in millis this feed and processor
     */
    Long getSuccessfulJobDuration();

    /**
     * set the duration in millis for those jobs that completed successfully
     */
    void setSuccessfulJobDuration(Long successfulJobDuration);

    /**
     * return the total processors failed by this feed and processor
     *
     * @return the total processors failed by this feed and processor
     */
    Long getProcessorsFailed();

    /**
     * set the number of processors which failed
     */
    void setProcessorsFailed(Long processorsFailed);

    /**
     * return the total flow files started by this feed and processor
     *
     * @return the total flow files started  by this feed and processor
     */
    Long getFlowFilesStarted();

    /**
     * set the number of flow files started
     */
    void setFlowFilesStarted(Long flowFilesStarted);

    /**
     * return the total flow files finished by this feed and processor
     *
     * @return the total flow files finished  by this feed and processor
     */
    Long getFlowFilesFinished();

    /**
     * set the number of flow files finished
     */
    void setFlowFilesFinished(Long flowFilesFinished);

    /**
     * return the collection time of these stats
     */
    DateTime getCollectionTime();

    /**
     * set the time for collecting these statistics
     */
    void setCollectionTime(DateTime collectionTime);

    /**
     * return the minimum event time for this feed and processor
     *
     * @return the minimum event time for this feed and processor
     */
    DateTime getMinEventTime();

    /**
     * set the minimum event time
     */
    void setMinEventTime(DateTime minEventTime);

    /**
     * return the minimum event time for this feed and processor
     *
     * @return the minimum event time for this feed and processor
     */
    Long getMinEventTimeMillis();

    /**
     * set the minimum event time
     */
    void setMinEventTimeMillis(Long minEventTimeMillis);

    /**
     * return the maximum event time for this feed and processor
     *
     * @return the maximum event time for this feed and processor
     */
    DateTime getMaxEventTime();

    /**
     * set the maximum event time
     */
    void setMaxEventTime(DateTime maxEventTime);

    /**
     * return the maximum event id for this feed and processor
     *
     * @return the maximum event id for this feed and processor
     */
    Long getMaxEventId();

    /**
     * set the maximum event id
     */
    void setMaxEventId(Long maxEventId);

    /**
     * return the cluster node id
     *
     * @return the cluster id
     */
    String getClusterNodeId();

    /**
     * set the cluster node id
     */
    void setClusterNodeId(String clusterNodeId);

    /**
     * return the cluster node address
     *
     * @return the cluster address
     */
    String getClusterNodeAddress();

    /**
     * set the cluster node address
     */
    void setClusterNodeAddress(String clusterNodeAddress);


    Long getCollectionIntervalSeconds();

    void setCollectionIntervalSeconds(Long collectionIntervalSeconds);


    BigDecimal getJobsStartedPerSecond();

    void setJobsStartedPerSecond(BigDecimal jobsStartedPerSecond);

    BigDecimal getJobsFinishedPerSecond();

    void setJobsFinishedPerSecond(BigDecimal jobsFinishedPerSecond);


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

    String getErrorMessages();

    DateTime getErrorMessageTimestamp();


}
