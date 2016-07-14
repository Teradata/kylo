/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;

import org.junit.BeforeClass;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAgreementClientTest {
    
    private static MetadataClient client;

    @BeforeClass
    public static void connect() {
        client = new MetadataClient(URI.create("http://localhost:8077/api/metadata/"));
    }

//    @Test
    public void testCreateSLA() {
        ServiceLevelAgreement sla = new ServiceLevelAgreement("TestSLA1",
                                                              FeedExecutedSinceFeedMetric.named("category", "FeedA", "category", "FeedX"),
                                                              FeedExecutedSinceFeedMetric.named("category", "FeedB", "category", "FeedX"));

        ServiceLevelAgreement result = client.createSla(sla);
        
        assertThat(result).isNotNull();
    }
}
