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
import com.thinkbiganalytics.scheduler.TriggerIdentifier;

/**
 * A default trigger identifier
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTriggerIdentifier extends DefaultScheduleIdentifier implements TriggerIdentifier {

    public DefaultTriggerIdentifier() {
        super();
    }

    public DefaultTriggerIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
        super(name, group);
    }
}
