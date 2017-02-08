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

/**
 * Links a given {@link NifiEvent} to a {@link com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution}
 */
public interface NifiEventStepExecution {

    /**
     * Return the NiFi event Id
     *
     * @return the event id
     */
    Long getEventId();

    /**
     * Return the NiFi FlowFileId
     *
     * @return the flowfile id
     */
    String getFlowFileId();

    /**
     * Return the NiFi Processor/Component id
     *
     * @return the NiFi processor/component id
     */
    String getComponentId();

    /**
     * Return the flow file for the start of this job.
     * This event could may be associated with a child flow file that points back to a parent flow file that started the job.  This is the reference back to the parent job.
     *
     * @return the job flow file id
     */
    String getJobFlowFileId();
}
