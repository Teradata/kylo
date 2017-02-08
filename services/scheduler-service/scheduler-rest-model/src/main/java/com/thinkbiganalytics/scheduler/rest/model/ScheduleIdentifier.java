package com.thinkbiganalytics.scheduler.rest.model;

/*-
 * #%L
 * thinkbig-scheduler-rest-model
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

/**
 * a schedule identifier for the user interface representing both JobIdentifier and TriggerIdentifiers
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleIdentifier {

    private String name;
    private String group = "DEFAULT";

    public ScheduleIdentifier() {
        super();
    }

    public ScheduleIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
