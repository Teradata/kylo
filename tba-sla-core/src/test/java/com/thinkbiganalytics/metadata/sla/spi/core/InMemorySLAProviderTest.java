package com.thinkbiganalytics.metadata.sla.spi.core;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

public class InMemorySLAProviderTest {
    
    private InMemorySLAProvider provider;

    @Before
    public void setUp() throws Exception {
        this.provider = new InMemorySLAProvider();
    }

    @Test
    public void testCreate() {
        ServiceLevelAgreement.ID id = this.provider.builder()
            .name("testCreateSLA")
            .description("testCreateSLA")
            .obligationBuilder()
                .description("testCreateSLA-o1")
                .metric(createTestMetric("o1-m1"))
                .add()
            .obligationBuilder()
                .description("testCreateSLA-o2")
                .metric(createTestMetric("o2-m1"))
                .metric(createTestMetric("o2-m2"))
                .add()
            .build().getId();
        
        assertThat(this.provider.getAgreements()).hasSize(1);
        
        ServiceLevelAgreement sla = this.provider.getAgreement(id);
        
        assertThat(sla.getName()).isEqualTo("testCreateSLA");
        assertThat(sla.getDescription()).isEqualTo("testCreateSLA");
        assertThat(sla.getObligations()).hasSize(2)
                                        .extracting("description")
                                        .contains("testCreateSLA-o1", "testCreateSLA-o2");
        assertThat(sla.getObligations()).flatExtracting("metrics")
                                        .hasSize(3)
                                        .extracting("description")
                                        .contains("o1-m1", "o2-m1", "o2-m2");
                                        
    }
    
    @Test
    public void testReplace() {
        ServiceLevelAgreement.ID id = this.provider.builder().name("First").build().getId();
        
        this.provider.builder(id)
            .name("Second")
            .build();
        
        ServiceLevelAgreement second = this.provider.getAgreement(id);
        
        assertThat(second).isNotNull();
        assertThat(second.getName()).isEqualTo("Second");
    }
    
    @Test
    public void testRemove() {
        ServiceLevelAgreement.ID id = this.provider.builder().name("short-timer").build().getId();
        
        this.provider.removeAgreement(id);
        
        assertThat(this.provider.getAgreements()).isEmpty();
        assertThat(this.provider.getAgreement(id)).isNull();
    }

    @Test
    public void testResolve() {
        ServiceLevelAgreement.ID id = this.provider.builder().name("sla").build().getId();
        
        ServiceLevelAgreement.ID fromStr = this.provider.resolve(id.toString());
        ServiceLevelAgreement.ID fromUUID = this.provider.resolve(UUID.fromString(id.toString()));
        ServiceLevelAgreement.ID fromID = this.provider.resolve(id);
        
        assertThat(id).isEqualTo(fromStr).isEqualTo(fromUUID).isEqualTo(fromID);
        assertThat(this.provider.getAgreement(fromStr)).isNotNull();
    }
    
    
    private Metric createTestMetric(final String descr) {
        return new Metric() {
            @Override
            public String getDescription() {
                return descr;
            }
        };
    }

}
