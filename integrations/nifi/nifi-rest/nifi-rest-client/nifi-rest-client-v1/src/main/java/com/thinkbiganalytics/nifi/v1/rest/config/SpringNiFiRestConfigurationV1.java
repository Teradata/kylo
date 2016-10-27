package com.thinkbiganalytics.nifi.v1.rest.config;

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1;
import com.thinkbiganalytics.nifi.v1.rest.model.NiFiPropertyDescriptorTransformV1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nonnull;

/**
 * Configures a {@link NiFiRestClient} for NiFi v1.0.
 */
@Configuration
@Profile("nifi-v1")
public class SpringNiFiRestConfigurationV1 {

    /**
     * Creates a new {@link NiFiRestClient}.
     *
     * @param nifiRestClientConfig the REST client configuration
     * @return the NiFi REST client
     */
    @Bean
    public NiFiRestClient nifiClient(@Nonnull final NifiRestClientConfig nifiRestClientConfig) {
        return new NiFiRestClientV1(nifiRestClientConfig);
    }

    /**
     * Creates a new {@link NiFiPropertyDescriptorTransform}.
     *
     * @return the NiFi PropertyDescriptor transform
     */
    @Bean
    public NiFiPropertyDescriptorTransform propertyDescriptorTransform() {
        return new NiFiPropertyDescriptorTransformV1();
    }
}
