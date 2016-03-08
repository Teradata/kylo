/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.event;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceChangeEvent {

    private Feed feed;
    private List<Dataset> datasets = new ArrayList<>();
    
    public DatasourceChangeEvent() {
        super();
    }
    
    public DatasourceChangeEvent(Feed feed) {
        super();
        this.feed = feed;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    public void addDataset(Dataset ds) {
        this.datasets.add(ds);
    }
}
