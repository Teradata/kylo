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

import com.thinkbiganalytics.metadata.sla.api.DuplicateAgreementNameException;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
            .build()
            .obligationBuilder()
            .description("testCreateSLA-o2")
            .metric(createTestMetric("o2-m1"))
            .metric(createTestMetric("o2-m2"))
            .build()
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

    @Test(expected = DuplicateAgreementNameException.class)
    public void testCreateDuplicateName() {
        this.provider.builder().name("testCreateSLA").build();
        this.provider.builder().name("testCreateSLA").build();
    }

    @Test
    public void testFindByName() {
        ServiceLevelAgreement sla = this.provider.builder()
            .name("testCreateSLA")
            .build();

        ServiceLevelAgreement found = this.provider.findAgreementByName(sla.getName());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(sla.getId());
        assertThat(found.getName()).isEqualTo(sla.getName());
    }

    @Test
    public void testReplace() {
        ServiceLevelAgreement.ID id = this.provider.builder().name("First").build().getId();

        this.provider.builder(id).name("Second").build();

        ServiceLevelAgreement second = this.provider.getAgreement(id);

        assertThat(second).isNotNull();
        assertThat(second.getName()).isEqualTo("Second");
    }

    @Test(expected = DuplicateAgreementNameException.class)
    public void testReplaceDuplicateName() {
        ServiceLevelAgreement.ID id = this.provider.builder().name("Initial").build().getId();

        this.provider.builder().name("Another").build();
        this.provider.builder(id).name("Another").build();
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
