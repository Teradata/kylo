/**
 * 
 */
package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * kylo-feed-manager-rest-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityVersionDifference {
    
    private EntityVersion fromVersion;
    private EntityVersion toVersion;
    private EntityDifference difference;

    public EntityVersionDifference() {
        super();
    }
    
    public EntityVersionDifference(EntityVersion fromVersion, EntityVersion toVersion, JsonNode patch) {
        this(fromVersion, toVersion, new EntityDifference(patch));
    }
    
    public EntityVersionDifference(EntityVersion fromVersion, EntityVersion toVersion, EntityDifference difference) {
        this();
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.difference = difference;
    }

    public EntityVersion getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(EntityVersion fromVersion) {
        this.fromVersion = fromVersion;
    }

    public EntityVersion getToVersion() {
        return toVersion;
    }

    public void setToVersion(EntityVersion toVersion) {
        this.toVersion = toVersion;
    }

    public EntityDifference getDifference() {
        return difference;
    }

    public void setDifference(EntityDifference difference) {
        this.difference = difference;
    }

}
