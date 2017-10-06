package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * kylo-feed-manager-rest-model
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The specification for a feed and how it should interact with various components.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedVersions {

    private String feedId;
    private List<EntityVersion> versions = new ArrayList<>();
    
    public FeedVersions() {
        super();
    }
    
    public FeedVersions(String feedId) {
        super();
        this.feedId = feedId;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public List<EntityVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<EntityVersion> versions) {
        this.versions = versions;
    }

    public EntityVersion addNewVersion(String id, String name, Date createdDate) {
        EntityVersion version = new EntityVersion(id, name, createdDate);
        this.versions.add(version);
        return version;
    }
}
