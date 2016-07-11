/**
 * 
 */
package com.thinkbiganalytics.metadata.core.sla;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.sla.TestMetric;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

/**
 *
 * @author Sean Felten
 */
public class TestMetricAssessor implements MetricAssessor<TestMetric, Serializable> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof TestMetric;
    }

    @Override
    public void assess(TestMetric metric, MetricAssessmentBuilder<Serializable> builder) {
        builder
            .metric(metric)
            .result(metric.getResult())
            .message(metric.getMessage());
    }

}
