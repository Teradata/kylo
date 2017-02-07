package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleServiceLevelAssessmentCompareTest {
    // @formatter:off

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
                    .build()
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
                    .build()
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
                    .build()
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
                    .build()
                .build();
        
        ServiceLevelAssessment assmt1 = this.assessor.assess(sla);
        obAssessor.setExpectedDescription("bbb");
        ServiceLevelAssessment assmt2 = this.assessor.assess(sla);
        
        assertThat(assmt1).isNotEqualByComparingTo(assmt2);
        assertThat(assmt1).isLessThan(assmt2);
    }
    
    // @formatter:on
}
