/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public interface MetricAssessor<M extends Metric> {

    boolean accepts(Metric metric);
    
    void assess(M metric, MetricAssessmentBuilder builder);
}
