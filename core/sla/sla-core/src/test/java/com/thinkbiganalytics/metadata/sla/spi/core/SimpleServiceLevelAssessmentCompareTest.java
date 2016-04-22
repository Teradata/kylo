package com.thinkbiganalytics.metadata.sla.spi.core;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

public class SimpleServiceLevelAssessmentCompareTest {
    
    private InMemorySLAProvider provider;
    
    private SimpleServiceLevelAssessor assessor;

    @Before
    public void setUp() throws Exception {
        this.provider = new InMemorySLAProvider();
        this.assessor = new SimpleServiceLevelAssessor();
    }


    @Test
    public void testCompareEqualMetricComparables() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .add()
                .build();
        
        ServiceLevelAssessment assmt1 = this.assessor.assess(sla);
        ServiceLevelAssessment assmt2 = this.assessor.assess(sla);
        
        assertThat(assmt1).isEqualByComparingTo(assmt2);
    }

    @Test
    public void testCompareEqualObligationComparables() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        this.assessor.registerObligationAssessor(new TestObligatinAssessor("test"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .add()
                .build();
        
        ServiceLevelAssessment assmt1 = this.assessor.assess(sla);
        ServiceLevelAssessment assmt2 = this.assessor.assess(sla);
        
        assertThat(assmt1).isEqualByComparingTo(assmt2);
    }
    
    @Test
    public void testCompareNotEqualMetricComparables() {
        TestMetric metric = new TestMetric(1, "1");
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(metric)
                    .add()
                .build();
        
        ServiceLevelAssessment assmt1 = this.assessor.assess(sla);
        metric.setIntValue(2);
        ServiceLevelAssessment assmt2 = this.assessor.assess(sla);
        
        assertThat(assmt1).isNotEqualByComparingTo(assmt2);
        assertThat(assmt1).isLessThan(assmt2);
    }
    
    @Test
    public void testCompareNotEqualObligationComparables() {
        TestObligatinAssessor obAssessor = new TestObligatinAssessor("aaa");
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        this.assessor.registerObligationAssessor(obAssessor);
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("aaa")
                    .metric(new TestMetric(1, "1"))
                    .add()
                .build();
        
        ServiceLevelAssessment assmt1 = this.assessor.assess(sla);
        obAssessor.setExpectedDescription("bbb");
        ServiceLevelAssessment assmt2 = this.assessor.assess(sla);
        
        assertThat(assmt1).isNotEqualByComparingTo(assmt2);
        assertThat(assmt1).isLessThan(assmt2);
    }
}
