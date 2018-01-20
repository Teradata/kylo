package com.thinkbiganalytics.nifi.v2.savepoint;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.reporting.InitializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Tags({"savepoint", "thinkbig", "kylo", "distributed"})
@CapabilityDescription("Provides a distributed savepoint service. A savepoint traps a flowfile pending a release signal enabling automatic retry of a pipeline sequence.")
public class DistributedSavepointController extends AbstractControllerService implements SavepointController {

    private SavepointProvider provider;

    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;

    // Identifies the distributed map cache client
    public static final PropertyDescriptor DISTRIBUTED_CACHE_SERVICE = new PropertyDescriptor.Builder()
        .name("distributed-cache-service")
        .displayName("Distributed Cache Service")
        .description("The Controller Service that is used to store savepoints")
        .required(true)
        .identifiesControllerService(DistributedMapCacheClient.class)
        .build();


    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        getLogger().info("Configuring Savepoint controller.");
        final DistributedMapCacheClient cache = context.getProperty(DISTRIBUTED_CACHE_SERVICE).asControllerService(DistributedMapCacheClient.class);
        this.provider = new DistributedSavepointProviderImpl(cache);
    }

    @Override
    protected void init(final ControllerServiceInitializationContext config) throws InitializationException {
        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(DISTRIBUTED_CACHE_SERVICE);
        this.properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public SavepointProvider getProvider() {
        return provider;
    }


}
