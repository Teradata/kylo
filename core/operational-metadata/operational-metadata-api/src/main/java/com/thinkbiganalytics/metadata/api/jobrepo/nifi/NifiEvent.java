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
 * Represents information about the NiFi Provenance event taken from the {@link com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO}
 */
@Deprecated
public interface NifiEvent {

    /**
     * Return the NiFi event ID
     * the {@link #getFlowFileId()}  + this eventId determine uniqueness of this record
     */
    Long getEventId();

    /**
     * Return the flowfile id for this event
     * the {@link #getEventId()} + this flowFileId determine uniqueness of this record
     */
    String getFlowFileId();

    /**
     * Return the name of the feed
     *
     * @return the name of the feed
     */
    String getFeedName();

    /**
     * Return the NiFi processor/component id
     *
     * @return the nifi processor/component id
     */
    String getProcessorId();

    /**
     * Return the display name of the processor
     *
     * @return the display name of the processor
     */
    String getProcessorName();

    /**
     * Return the process group id for the feed that this event partakes in
     */
    String getFeedProcessGroupId();

    String getEventDetails();

    DateTime getEventTime();

    String getFileSize();

    Long getFileSizeBytes();

    String getParentFlowFileIds();

    String getChildFlowFileIds();

    String getAttributesJson();

    String getSourceConnectionId();

    String getEventType();

    Long getEventDuration();

    String getJobFlowFileId();

    boolean isStartOfJob();

    boolean isEndOfJob();

    boolean isFailure();

    boolean isBatchJob();

    boolean isFinalJobEvent();

    boolean isHasFailureEvents();

    String getClusterNodeId();

    String getClusterNodeAddress();

}
