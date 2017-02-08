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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

/**
 * Links a given {@link NifiEvent} to a {@link BatchJobExecution}
 */
public interface NifiEventJobExecution {

    /**
     * Return the batch job execution associated with this event
     */
    BatchJobExecution getJobExecution();

    /**
     * Return the nifi event id
     *
     * @return the event id
     */
    Long getEventId();

    /**
     * set the nifi event id
     */
    void setEventId(Long eventId);

    /**
     * Return the nifi flowfile id
     *
     * @return the nifi flowfile id
     */
    String getFlowFileId();
}
