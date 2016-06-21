/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Obligation {

    private String description;
    private List<Metric> metrics;

    public Obligation() {
    }
    
    public Obligation(String description, Metric... metrics) {
        this(description, Arrays.asList(metrics));
    }

    public Obligation(String description, List<Metric> metrics) {
        super();
        this.description = description;
        this.metrics = metrics;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

}
