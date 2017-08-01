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
