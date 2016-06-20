/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;

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
                                        FeedExecutedSinceFeedMetric.named("FeedA", "FeedX"),
                                        FeedExecutedSinceFeedMetric.named("FeedB", "FeedX"));

        ServiceLevelAgreement result = client.createSla(sla);
        
        assertThat(result).isNotNull();
    }
}
