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

import org.joda.time.DateTime;


/**
 * Created by sr186054 on 12/2/16.
 */
public interface JobStatusCount {

    Long getCount();

    void setCount(Long count);

    String getFeedName();

    void setFeedName(String feedName);

    String getJobName();

    void setJobName(String jobName);

    String getStatus();

    void setStatus(String status);

    DateTime getDate();

    void setDate(DateTime date);

}
