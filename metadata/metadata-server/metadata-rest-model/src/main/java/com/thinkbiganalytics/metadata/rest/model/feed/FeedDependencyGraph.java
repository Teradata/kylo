/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "feed", "preconditonResult", "dependencies" })
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
