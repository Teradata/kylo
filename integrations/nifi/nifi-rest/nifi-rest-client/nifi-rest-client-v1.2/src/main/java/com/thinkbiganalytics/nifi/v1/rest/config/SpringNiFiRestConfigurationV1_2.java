package com.thinkbiganalytics.nifi.v1.rest.config;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1_2;
import com.thinkbiganalytics.nifi.v1.rest.model.NiFiPropertyDescriptorTransformV1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Nonnull;

/**
 * Configures a {@link NiFiRestClient} for NiFi v1.2+.
 */
@Configuration
@Profile({"nifi-v1.2","nifi-v1.3","nifi-v1.4"})
public class SpringNiFiRestConfigurationV1_2 {

    /**
     * Creates a new {@link NiFiRestClient}.
     *
     * @param nifiRestClientConfig the REST client configuration
     * @return the NiFi REST client
     */
    @Bean
    public NiFiRestClient nifiClient(@Nonnull final NifiRestClientConfig nifiRestClientConfig) {
        return new NiFiRestClientV1_2(nifiRestClientConfig);
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
