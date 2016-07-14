package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.metadata.sla.api.Metric;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 7/13/16.
 */
public class DefaultPreconditionGroup implements PreconditionGroup {

    private Set<Metric> metrics;
    private String condition;

    public DefaultPreconditionGroup(Set<Metric> metrics, String condition) {
        this.metrics = metrics;
        this.condition = condition;
    }

    public DefaultPreconditionGroup(Metric metric, String condition) {
        this.metrics = new HashSet<>();
        metrics.add(metric);
        this.condition = condition;
    }

    public Set<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
