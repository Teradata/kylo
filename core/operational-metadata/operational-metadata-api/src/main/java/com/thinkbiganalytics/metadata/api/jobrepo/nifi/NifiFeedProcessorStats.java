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
     * @param processorName
     */
    void setProcessorName(String processorName);

    /**
     * Return the feed name
     * @return the feed name
     */
    String getFeedName();

    /**
     * set the feed name
     * @param feedName
     */
    void setFeedName(String feedName);

    /**
     * return the processor id
     * @return
     */
    String getProcessorId();

    /**
     * set the processor id
     * @param processorId
     */
    void setProcessorId(String processorId);

    /**
     * Return  the unique id  for this feed processor statistic entry
     * @return the unique id  for this feed processor statistic entry
     */
    String getId();

    /**
     * set the unique id for this entry
     * @param id
     */
    void setId(String id);

    /**
     * Return the feed process group id
     * @return the feed process group id
     */
    String getFeedProcessGroupId();

    /**
     * Set the feed process group id
     * @param feedProcessGroupId
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
     * @param collectionId
     */
    void setCollectionId(String collectionId);

    /**
     * Return the sum of the duration in millis of events for this feed/processor during its collection
     * @return the sum of the duration in millis of events for this feed/processor during its collection
     */
    Long getDuration();

    /**
     * set the duration in millis
     * @param duration
     */
    void setDuration(Long duration);

    /**
     * return the total bytes taken in by this feed and processor
     * @return the total bytes taken in by this feed and processor
     */
    Long getBytesIn();

    /**
     * set the total bytes taken in by this processor
     * @param bytesIn
     */
    void setBytesIn(Long bytesIn);

    /**
     * return the total bytes out by this feed and processor
     * @return the total bytes out by this feed and processor
     */
    Long getBytesOut();

    /**
     * set the total bytes out
     * @param bytesOut
     */
    void setBytesOut(Long bytesOut);

    /**
     * return the total events for this feed and processor
     * @return the total events for  this feed and processor
     */
    Long getTotalCount();

    /**
     * set the total event count
     * @param totalCount
     */
    void setTotalCount(Long totalCount);

    /**
     * return the total jobs started by this feed and processor
     * @return the total jobs started by this feed and processor
     */
    Long getJobsStarted();

    /**
     * set the jobs started
     * @param jobsStarted
     */
    void setJobsStarted(Long jobsStarted);

    /**
     * return the total jobs finished by this feed and processor
     * @return the total jobs finished by this feed and processor
     */
    Long getJobsFinished();

    /**
     * set the jobs finished
     * @param jobsFinished
     */
    void setJobsFinished(Long jobsFinished);

    /**
     * return the total jobs failed by this feed and processor
     * @return the total jobs failed by this feed and processor
     */
    Long getJobsFailed();

    /**
     * set the jobs failed
     * @param jobsFailed
     */
    void setJobsFailed(Long jobsFailed);

    /**
     * return the total job duration in millis for this feed and processor
     * @return the total job duration in millis for this feed and processor
     */
    Long getJobDuration();

    /**
     * set the job duration in millis
     * @param jobDuration
     */
    void setJobDuration(Long jobDuration);

    /**
     * return the successful job duration in millis for this feed and processor
     * @return the total ob duration in millis this feed and processor
     */
    Long getSuccessfulJobDuration();

    /**
     * set the duration in millis for those jobs that completed successfully
     * @param successfulJobDuration
     */
    void setSuccessfulJobDuration(Long successfulJobDuration);

    /**
     * return the total processors failed by this feed and processor
     * @return the total processors failed by this feed and processor
     */
    Long getProcessorsFailed();

    /**
     * set the number of processors which failed
     * @param processorsFailed
     */
    void setProcessorsFailed(Long processorsFailed);

    /**
     * return the total flow files started by this feed and processor
     * @return the total flow files started  by this feed and processor
     */
    Long getFlowFilesStarted();

    /**
     * set the number of flow files started
     * @param flowFilesStarted
     */
    void setFlowFilesStarted(Long flowFilesStarted);

    /**
     * return the total flow files finished by this feed and processor
     * @return the total flow files finished  by this feed and processor
     */
    Long getFlowFilesFinished();

    /**
     * set the number of flow files finished
     * @param flowFilesFinished
     */
    void setFlowFilesFinished(Long flowFilesFinished);

    /**
     * return the collection time of these stats
     * @return
     */
    DateTime getCollectionTime();

    /**
     * set the time for collecting these statistics
     * @param collectionTime
     */
    void setCollectionTime(DateTime collectionTime);

    /**
     * return the minimum event time for this feed and processor
     * @return the minimum event time for this feed and processor
     */
    DateTime getMinEventTime();

    /**
     * set the minimum event time
     * @param minEventTime
     */
    void setMinEventTime(DateTime minEventTime);

    /**
     * return the maximum event time for this feed and processor
     * @return the maximum event time for this feed and processor
     */
    DateTime getMaxEventTime();

    /**
     * set the maximum event time
     * @param maxEventTime
     */
    void setMaxEventTime(DateTime maxEventTime);

    /**
     * return the maximum event id for this feed and processor
     * @return the maximum event id for this feed and processor
     */
    Long getMaxEventId();

    /**
     * set the maximum event id
     * @param maxEventId
     */
    void setMaxEventId(Long maxEventId);

    /**
     * return the cluster node id
     * @return the cluster id
     */
    String getClusterNodeId();

    /**
     * set the cluster node id
     * @param clusterNodeId
     */
    void setClusterNodeId(String clusterNodeId);

    /**
     * return the cluster node address
     * @return the cluster address
     */
    String getClusterNodeAddress();

    /**
     * set the cluster node address
     * @param clusterNodeAddress
     */
    void setClusterNodeAddress(String clusterNodeAddress);


}
