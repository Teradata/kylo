/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"feed", "preconditonResult", "dependencies"})
public class FeedDependencyGraph {

    private Feed feed;
    private ServiceLevelAssessment preconditonResult;
    private List<FeedDependencyGraph> dependencies = new ArrayList<>();

    public FeedDependencyGraph() {
    }

    public FeedDependencyGraph(Feed feed, ServiceLevelAssessment preconditonResult) {
        super();
        this.feed = feed;
        this.preconditonResult = preconditonResult;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public ServiceLevelAssessment getPreconditonResult() {
        return preconditonResult;
    }

    public void setPreconditonResult(ServiceLevelAssessment preconditonResult) {
        this.preconditonResult = preconditonResult;
    }

    public List<FeedDependencyGraph> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<FeedDependencyGraph> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependecy(FeedDependencyGraph dep) {
        this.dependencies.add(dep);
    }
}
