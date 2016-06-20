package com.thinkbiganalytics.metadata.sla.spi.core;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.AssessorNotFoundException;

public class SimpleServiceLevelAssessorTest {
    
    private InMemorySLAProvider provider;
    
    private SimpleServiceLevelAssessor assessor;

    @Before
    public void setUp() throws Exception {
        this.provider = new InMemorySLAProvider();
        this.assessor = new SimpleServiceLevelAssessor();
    }

    @Test
    public void testAssessMetricOnlySuccess() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
    }
    
    @Test
    public void testAssessAllAssessorsSuccess() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        this.assessor.registerObligationAssessor(new TestObligatinAssessor());
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
    }

    @Test
    public void testAssessMetricFailure() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(0, "fail"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.FAILURE);
    }
    
    @Test
    public void testAssessMetricWaring() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(0, "fail", AssessmentResult.WARNING));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.WARNING);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.WARNING);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.WARNING);
    }
    
    @Test
    public void testAssessObligationFailureMetricWaring() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(0, "fail", AssessmentResult.WARNING));
        this.assessor.registerObligationAssessor(new TestObligatinAssessor("fail"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.WARNING);
    }
    
    @Test(expected=AssessorNotFoundException.class)
    public void testAssessNoMetricAssessor() {
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationBuilder()
                    .description("test")
                    .metric(new TestMetric(1, "1"))
                    .build()
                .build();
        
        this.assessor.assess(sla);
    }
    
    @Test
    public void testAssessSufficientRequiredSuccess1st() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(1, "1"))
                        .build()
                    .build()
                .obligationGroupBuilder(Condition.REQUIRED)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(0, "fail"))
                        .build()
                    .build()    
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.SUCCESS);
    }

    @Test
    public void testAssessRequiredSufficientFailure1st() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationGroupBuilder(Condition.REQUIRED)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(0, "fail"))
                        .build()
                    .build()
                .obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(1, "1"))
                        .build()
                    .build()    
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.FAILURE);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(1)
            .extracting("result")
            .contains(AssessmentResult.FAILURE);
    }

    @Test
    public void testAssessSufficientSufficientFailure1stSuccess2nd() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(0, "fail"))
                        .build()
                    .build()
                .obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(1, "1"))
                        .build()
                    .build()    
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.SUCCESS);
    }

    @Test
    public void testAssessRequiredSufficientSuccess1stFailure2nd() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1"));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                    .obligationGroupBuilder(Condition.REQUIRED)
                        .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(1, "1"))
                        .build()
                    .build()
                .obligationGroupBuilder(Condition.SUFFICIENT)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(0, "fail"))
                        .build()
                    .build()    
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.SUCCESS);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.SUCCESS);
    }

    @Test
    public void testAssessOptionalOptionalFailure1stWarning2nd() {
        this.assessor.registerMetricAssessor(new TestMetricAssessor(1, "1", AssessmentResult.FAILURE, AssessmentResult.WARNING));
        
        ServiceLevelAgreement sla = this.provider.builder()
                .name("test")
                .obligationGroupBuilder(Condition.OPTIONAL)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(0, "fail"))
                        .build()
                    .build()
                .obligationGroupBuilder(Condition.OPTIONAL)
                    .obligationBuilder()
                        .description("test")
                        .metric(new TestMetric(1, "1"))
                        .build()
                    .build()    
                .build();
        
        ServiceLevelAssessment assessment = this.assessor.assess(sla);
        
        assertThat(assessment.getResult()).isEqualTo(AssessmentResult.WARNING);
        assertThat(assessment.getObligationAssessments())
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.WARNING);
        assertThat(assessment.getObligationAssessments())
            .flatExtracting("metricAssessments")
            .hasSize(2)
            .extracting("result")
            .contains(AssessmentResult.FAILURE, AssessmentResult.WARNING);
    }

}
