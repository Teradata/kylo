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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.scheduler.ScheduleIdentifier;
import com.thinkbiganalytics.scheduler.support.IdentifierUtil;

/**
 * a base identifier for jobs and triggers
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultScheduleIdentifier implements ScheduleIdentifier {

    private String name;
    private String group = "DEFAULT";


    public DefaultScheduleIdentifier() {
        this.name = createUniqueName(this.group);
    }

    public DefaultScheduleIdentifier(@JsonProperty("name") String name, @JsonProperty("group") String group) {
        this.name = name;
        this.group = group;
        if (this.group == null || this.group.trim().equals("")) {
            this.group = "DEFAULT";
        }
    }

    @JsonIgnore
    public static String createUniqueName(String item) {
        return IdentifierUtil.createUniqueName(item);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    @JsonIgnore
    public String getUniqueName() {
        return name + "_" + DefaultScheduleIdentifier.createUniqueName(this.group);
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + (this.group == null ? 0 : this.group.hashCode());
        result1 = 31 * result1 + (this.name == null ? 0 : this.name.hashCode());
        return result1;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            DefaultScheduleIdentifier other = (DefaultScheduleIdentifier) obj;
            if (this.group == null) {
                if (other.group != null) {
                    return false;
                }
            } else if (!this.group.equals(other.group)) {
                return false;
            }

            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!this.name.equals(other.name)) {
                return false;
            }

            return true;
        }
    }


    public int compareTo(ScheduleIdentifier o) {
        if (this.group.equals("DEFAULT") && !o.getGroup().equals("DEFAULT")) {
            return -1;
        } else if (!this.group.equals("DEFAULT") && o.getGroup().equals("DEFAULT")) {
            return 1;
        } else {
            int r = this.group.compareTo(o.getGroup());
            return r != 0 ? r : this.name.compareTo(o.getName());
        }
    }

}
