/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 * Thrown when an assessor could not be found that can assess a particular kind of metric.
 * @author Sean Felten
 */
public class AssessorNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 3406314751186303643L;
    
    private final Metric metric;
    
    public AssessorNotFoundException(Metric metric) {
        this.metric = metric;
    }

    public AssessorNotFoundException(String message, Metric metric) {
        super(message);
        this.metric = metric;
    }

    public Metric getMetric() {
        return metric;
    }
}
