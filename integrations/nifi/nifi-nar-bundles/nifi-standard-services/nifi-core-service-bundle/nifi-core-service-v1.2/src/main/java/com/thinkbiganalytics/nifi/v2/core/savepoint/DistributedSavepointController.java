package com.thinkbiganalytics.nifi.v2.core.savepoint;

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

import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointReplayEventConsumer;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointReplayEventListener;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayEvent;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.reporting.InitializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Tags({"savepoint", "thinkbig", "kylo", "distributed"})
@CapabilityDescription("Provides a distributed savepoint service. A savepoint traps a flowfile pending a release signal enabling automatic retry of a pipeline sequence.")
public class DistributedSavepointController extends AbstractControllerService implements SavepointController {

    private SavepointProvider provider;

    private SavepointReplayEventListener savepointReplayEventListener = new DefaultSavepointReplayEventListener();


    private SavepointProcessorCache cache = new SavepointProcessorCache();

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

    public static final PropertyDescriptor SPRING_SERVICE = new PropertyDescriptor.Builder()
        .name("Spring Context Service")
        .description("Service for loading spring a spring context and providing bean lookup")
        .required(true)
        .identifiesControllerService(SpringContextService.class)
        .build();

    private SpringContextService springService;


    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        getLogger().info("Configuring Savepoint controller.");
        final DistributedMapCacheClient cacheClient = context.getProperty(DISTRIBUTED_CACHE_SERVICE).asControllerService(DistributedMapCacheClient.class);
        this.provider = new DistributedSavepointProviderImpl(cacheClient);
        this.provider.subscribeDistributedSavepointChanges(this.cache);
        this.springService = context.getProperty(SPRING_SERVICE).asControllerService(SpringContextService.class);
        addJmsListeners();
    }

    @Override
    protected void init(final ControllerServiceInitializationContext config) throws InitializationException {
        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(DISTRIBUTED_CACHE_SERVICE);
        props.add(SPRING_SERVICE);
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


    private SavepointReplayResponseJmsProducer getSavepointReplayResponseJmsProduce() {
        return this.springService.getBean(SavepointReplayResponseJmsProducer.class);
    }

    private void addJmsListeners() {
        SavepointReplayEventConsumer consumer = this.springService.getBean(SavepointReplayEventConsumer.class);
        if (consumer != null) {
            consumer.addListener(savepointReplayEventListener);
        }
    }

    private void removeJmsListeners() {
        SavepointReplayEventConsumer consumer = this.springService.getBean(SavepointReplayEventConsumer.class);
        if (consumer != null) {
            consumer.removeListener(savepointReplayEventListener);
        }
    }


    /**
     * Add the flow file back to processing
     *
     * @param processorId the processor id
     * @param flowfileId  the flow file to add back to the cache
     */
    public void putFlowfileBack(String processorId, String flowfileId) {
        cache.putFlowfileBack(processorId, flowfileId);
    }

    public Optional<String> getNextFlowFile(String processorId) throws CacheNotInitializedException {
        return cache.getNextFlowFile(processorId);
    }

    public Optional<String> initializeAndGetNextFlowFile(String processorId, Collection<String> newFlowfiles, Collection<SavepointEntry> savepointEntries) {
        newFlowfiles.stream().forEach(e -> cache.putFlowfile(processorId, e));
        savepointEntries.stream().forEach(e -> cache.putSavepoint(e));
        cache.markInitialized(processorId);
        try {
            return getNextFlowFile(processorId);
        } catch (CacheNotInitializedException e) {
            return Optional.empty();
        }
    }

    private class DefaultSavepointReplayEventListener implements SavepointReplayEventListener {

        private int maxRetries = 10;

        private void lockAndProcess(SavepointReplayEvent event, String savepointId, int attempt) {
            Lock lock = null;
            try {
                lock = provider.lock(savepointId);
                if (lock != null) {
                    if (event.getAction() == SavepointReplayEvent.Action.RETRY) {
                        getLogger().info("Attempt to retry savepoint: {}, flowfile: {}, lock: {} ", new Object[]{savepointId, event.getFlowfileId(), lock});
                        provider.retry(savepointId, lock);
                        getLogger().info("Successfully retried savepoint: {}, flowfile: {}, lock: {} ", new Object[]{savepointId, event.getFlowfileId(), lock});
                        getSavepointReplayResponseJmsProduce().replaySuccess(event, "Successfully retried the savepoint " + savepointId);
                    } else {
                        getLogger().info("Attempt to force release savepoint: {}, flowfile: {}, lock: {} ", new Object[]{savepointId, event.getFlowfileId(), lock});
                        provider.release(savepointId, lock, false);
                        getLogger().info("Successfully to released savepoint: {}, flowfile: {}, lock: {} ", new Object[]{savepointId, event.getFlowfileId(), lock});
                        getSavepointReplayResponseJmsProduce().replaySuccess(event, "Successfully released the savepoint " + savepointId);
                    }
                } else {
                    getLogger().info("Unable to get a Lock for {} ", new Object[]{savepointId});
                    getSavepointReplayResponseJmsProduce().replayFailure(event, "Unable to obtain lock for savepoint " + savepointId);
                }


            } catch (InvalidLockException | InvalidSetpointException e) {
                getLogger().info("Exception while attempting to {} the savepoint using the flow file: {}.  Retry Attempt: {} out of {}",
                                 new Object[]{event.getAction(), event.getFlowfileId(), attempt, maxRetries});
                if (attempt < maxRetries) {
                    attempt++;
                    lockAndProcess(event, savepointId, attempt);
                } else {
                    //log and exit
                    getLogger().info("MAX retries reached.  Exception while attempting to {} the savepoint using the flow file: {}.  Retry Attempt: {} out of {}",
                                     new Object[]{event.getAction(), event.getFlowfileId(), attempt, maxRetries});
                    getSavepointReplayResponseJmsProduce().replayFailure(event, "Unable to obtain lock for savepoint " + savepointId + ". Maximum replay retries reached");
                }

            } catch (IOException e) {
                getLogger().info("Unable to obtain lock for {} ", new Object[]{event});
                getSavepointReplayResponseJmsProduce().replayFailure(event, "Unable to obtain lock for savepoint " + savepointId + ". Exception: " + e.getMessage());
            } finally {
                if (lock != null) {
                    try {
                        provider.unlock(lock);
                    } catch (IOException e) {
                        getLogger().warn("Unable to unlock {}", new String[]{savepointId});
                    }
                }
            }

        }

        @Override
        public void triggered(SavepointReplayEvent event) {

            String savepointId = provider.resolveByFlowFileUUID(event.getFlowfileId());
            if (savepointId != null && !("").equalsIgnoreCase(savepointId)) {
                //lock
                getLogger().info("Savepoint message TRIGGERED for request {}.  Savepoint {} found.  Attempting to Lock and process request. ", new Object[]{event, savepointId});
                lockAndProcess(event, savepointId, 1);
            } else {
                getLogger().info("Savepoint message trigger ABORTED for request: {}.  Savepoint was not found for the flowfile.  No futher action will happen ", new Object[]{event});
                getSavepointReplayResponseJmsProduce().replayFailure(event, "Savepoint was not found for the flowfile");
            }

        }
    }

}
