package com.thinkbiganalytics.nifi.v0.rest.config;

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.v0.rest.client.NiFiRestClientV0;
import com.thinkbiganalytics.nifi.v0.rest.model.NiFiPropertyDescriptorTransformV0;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nonnull;

/**
 * Configures a {@link NiFiRestClient} for NiFi v0.6.
 */
@Configuration
@Profile("nifi-v0")
public class SpringNiFiRestConfigurationV0 {

    /**
     * Creates a new {@link NiFiRestClient}.
     *
     * @param nifiRestClientConfig the REST client configuration
     * @return the NiFi REST client
     */
    @Bean
    public NiFiRestClient nifiClient(@Nonnull final NifiRestClientConfig nifiRestClientConfig) {
        return new NiFiRestClientV0(nifiRestClientConfig);
    }

    /**
     * Creates a new {@link NiFiPropertyDescriptorTransform}.
     *
     * @return the NiFi PropertyDescriptor transform
     */
    @Bean
    public NiFiPropertyDescriptorTransform propertyDescriptorTransform() {
        return new NiFiPropertyDescriptorTransformV0();
    }
}
