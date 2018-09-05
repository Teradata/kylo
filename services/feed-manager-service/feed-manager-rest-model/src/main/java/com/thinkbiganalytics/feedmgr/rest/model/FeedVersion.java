/**
 * 
 */
package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * kylo-feed-manager-rest-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import java.util.Date;

/**
 * Represents a version of a feed.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedVersion extends EntityVersion {
    
    private boolean deployed = false;

    public FeedVersion() {
    }

    public FeedVersion(String id, String name, Date createdDate, boolean isDeployed) {
        this(id, name, createdDate, isDeployed, null);
    }

    public FeedVersion(String id, String name, Date createdDate, boolean isDeployed, Object entity) {
        super(id, name, createdDate, entity);
        this.deployed = isDeployed;
    }

    /**
     * @return the deployed
     */
    public boolean isDeployed() {
        return deployed;
    }
    
    /**
     * @param deployed the deployed to set
     */
    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }
}
