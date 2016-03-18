/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPrecondition implements Serializable {

    private List<List<Metric>> metricGroups = new ArrayList<>();

    public FeedPrecondition() {
        super();
    }
    
    public FeedPrecondition(List<Metric> metrics) {
        this.addMetrics(metrics);
    }
    
    public void addMetrics(Metric... metrics) {
        addMetrics(Arrays.asList(metrics));
    }
    
    public void addMetrics(List<Metric> metrics) {
        List<Metric> list = new ArrayList<Metric>(metrics);
        this.metricGroups.add(list);
    }

    public List<List<Metric>> getMetricGroups() {
        return metricGroups;
    }

    public void setMetricGroups(List<List<Metric>> metrics) {
        this.metricGroups = new ArrayList<>(metrics);
    }
}
