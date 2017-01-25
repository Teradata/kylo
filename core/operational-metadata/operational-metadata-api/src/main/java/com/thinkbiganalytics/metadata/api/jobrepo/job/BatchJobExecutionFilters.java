package com.thinkbiganalytics.metadata.api.jobrepo.job;

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
 * Created by sr186054 on 12/1/16.
 */
public interface BatchJobExecutionFilters {

    String RUNNING_FILTER = "endTime==NULL";
    String ABANDONED_FILTER = "status=="+BatchJobExecution.JobStatus.ABANDONED.name();
    String COMPLETED_FILTER = "status=="+BatchJobExecution.JobStatus.COMPLETED.name();
    String STARTED_FILTER = "status=="+BatchJobExecution.JobStatus.STARTED.name();
    String STOPPED_FILTER = "status=="+BatchJobExecution.JobStatus.STOPPED.name();
    String FAILED_FILTER = "status=="+BatchJobExecution.JobStatus.FAILED.name();
    String RUNNING_OR_FAILED_FILTER = "status==\""+BatchJobExecution.JobStatus.STARTED.name()+","+BatchJobExecution.JobStatus.STARTING.name()+","+BatchJobExecution.JobStatus.FAILED.name()+"\"";

}
