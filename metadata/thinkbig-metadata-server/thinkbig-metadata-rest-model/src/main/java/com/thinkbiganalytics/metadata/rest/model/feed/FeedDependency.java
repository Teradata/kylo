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
@JsonPropertyOrder({ "feed", "preconditonResult", "destination", "dependencies" })
public class FeedDependency {

    private Feed feed;
    private ServiceLevelAssessment preconditonResult;
    private Datasource destination;
    private List<FeedDependency> dependencies = new ArrayList<>();
    
    public FeedDependency() {
    }

    public FeedDependency(Feed feed, Datasource destination, ServiceLevelAssessment preconditonResult) {
        super();
        this.feed = feed;
        this.destination = destination;
        this.preconditonResult = preconditonResult;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Datasource getDestination() {
        return destination;
    }

    public void setDestination(Datasource destination) {
        this.destination = destination;
    }

    public ServiceLevelAssessment getPreconditonResult() {
        return preconditonResult;
    }

    public void setPreconditonResult(ServiceLevelAssessment preconditonResult) {
        this.preconditonResult = preconditonResult;
    }

    public List<FeedDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<FeedDependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public void addDependecy(FeedDependency dep) {
        this.dependencies.add(dep);
    }
}
