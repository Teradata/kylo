package com.thinkbiganalytics.scheduler.model;

/*-
 * #%L
 * thinkbig-scheduler-core
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.JobIdentifier;

/**
 * a Job identifier
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultJobIdentifier extends DefaultScheduleIdentifier implements JobIdentifier {

    public DefaultJobIdentifier() {
        super();
    }

    public DefaultJobIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
        super(name, group);
    }
}
