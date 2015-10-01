/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor.MetricContext;
import com.thinkbiganalytics.metadata.sla.spi.mock.MockSLAProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class MockSLAConfiguration {
    
    @Bean(name="slaProvider")
    public ServiceLevelAgreementProvider slaProvider() {
        return new MockSLAProvider();
    }
    
    @Bean(name="exampleSLA1")
    public ServiceLevelAgreement exampleSLA1() {
        return slaProvider().builder()
            .name("Example ServiceLevelAgreement #1")
            .description("This is an example of an ServiceLevelAgreement with 1 obligation with 1 metric")
            .obligationBuilder()
                .description("An obligation that must be met")
                .metric(testMetric1())
                .add()
            .build();
    }

    @Bean(name="testMetric1")
    public Metric testMetric1() {
        return new TestMetric(true, "This metric alwasy indicates success");
    }
    
    @Bean(name="testMetricAssessor")
    public MetricAssessor<TestMetric> testMetricAssessor() {
        return new MetricAssessor<TestMetric>() {
            @Override
            public boolean accepts(Metric metric) {
                return metric instanceof TestMetric;
            }
            
            @Override
            public void assess(TestMetric metric, MetricContext context, MetricAssessmentBuilder builder) {
                if (metric.isSuccess()) {
                    builder
                        .message("This metric was a success")
                        .result(AssessmentResult.SUCCESS)
                        .metric(metric);
                } else {
                    builder
                        .message("This metric was a failure")
                        .result(AssessmentResult.FAILURE)
                        .metric(metric);
                }
            }
        };
    }

}
