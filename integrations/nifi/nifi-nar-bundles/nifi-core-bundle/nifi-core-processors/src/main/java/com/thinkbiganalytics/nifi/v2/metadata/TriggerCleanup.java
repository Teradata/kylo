package com.thinkbiganalytics.nifi.v2.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventService;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Listens for a {@link FeedCleanupTriggerEvent} from JMS and generates a {@link FlowFile} with the feed's properties.
 */
@CapabilityDescription("Listens for the feed to be deleted in the Feed Manager and generates a FlowFile with the feed properties.")
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"cleanup", "trigger", "thinkbig"})
public class TriggerCleanup extends AbstractNiFiProcessor implements CleanupListener {

    /**
     * Property for the category system name
     */
    public static final PropertyDescriptor CATEGORY_NAME = new PropertyDescriptor.Builder()
        .name("System feed category")
        .description("The category name of this feed. The default is to have this name automatically set when the feed is created. Normally you do not need to change the default value.")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("${metadata.category.systemName}")
        .expressionLanguageSupported(false)
        .required(true)
        .build();

    /**
     * Property for the cleanup event service
     */
    public static final PropertyDescriptor CLEANUP_SERVICE = new PropertyDescriptor.Builder()
        .name("Feed Cleanup Event Service")
        .description("Service that manages the cleanup of feeds.")
        .identifiesControllerService(CleanupEventService.class)
        .required(true)
        .build();

    /**
     * Property for the metadata provider service
     */
    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Provider Service")
        .description("Service supplying the implementations of the various metadata providers.")
        .identifiesControllerService(MetadataProviderService.class)
        .required(true)
        .build();

    /**
     * Property for the feed system name
     */
    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System feed name")
        .description("The system name of this feed. The default is to have this name automatically set when the feed is created. Normally you do not need to change the default value.")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("${metadata.systemFeedName}")
        .expressionLanguageSupported(false)
        .required(true)
        .build();

    /**
     * Relationship for transferring {@code FlowFile}s generated from events
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("Success")
        .description("Relationship followed on successful precondition event.")
        .build();

    /**
     * List of property descriptors
     */
    private static final List<PropertyDescriptor> properties = ImmutableList.of(CLEANUP_SERVICE, METADATA_SERVICE, CATEGORY_NAME, FEED_NAME);

    /**
     * List of relationships
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(REL_SUCCESS);
    /**
     * List of events to process
     */
    @Nonnull
    private final Queue<FeedCleanupTriggerEvent> queue = new LinkedBlockingQueue<>();
    /**
     * Identifier for this feed
     */
    @Nullable
    private String category;

    /**
     * Identifier for this feed
     */
    @Nullable
    private String feed;

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Initializes resources required to trigger this processor.
     *
     * @param context the process context
     */
    @OnScheduled
    public void onScheduled(@Nonnull final ProcessContext context) {
        getLog().debug("Scheduled");

        // Get the feed id
        category = context.getProperty(CATEGORY_NAME).getValue();
        feed = context.getProperty(FEED_NAME).getValue();
        getLog().debug("Scheduled for {}.{}", new Object[]{category, feed});

        // Listen for cleanup events
        getCleanupService(context).addListener(category, feed, this);
    }

    @Override
    public void onTrigger(@Nonnull final ProcessContext context, @Nonnull final ProcessSession session) throws ProcessException {
        getLog().trace("Triggered for feed {}.{}", new Object[]{category, feed});
        // Look for an event to process
        FeedCleanupTriggerEvent event = queue.poll();
        if (event == null) {
            getLog().trace("Triggered, but no message in queue");
            context.yield();
            return;  // nothing to do
        }

        String feedId;
        try {
            feedId = getMetadataService(context).getProvider().getFeedId(category, feed);
            getLog().debug("Triggered for feed " + feedId);
        } catch (Exception e) {
            getLog().error("Failure retrieving metadata for feed: {}.{}", new Object[]{category, feed}, e);
            throw new IllegalStateException("Failed to retrieve feed metadata", e);
        }

        // Verify feed properties
        Properties properties = (feedId != null) ? getMetadataService(context).getProvider().getFeedProperties(feedId) : null;
        getLog().debug("Feed properties " + properties);

        if (properties == null) {
            throw new IllegalStateException("Failed to fetch properties for feed: " + feedId);
        }
        if (!properties.containsKey(FeedProperties.CLEANUP_ENABLED) || !"true".equals(properties.getProperty(FeedProperties.CLEANUP_ENABLED))) {
            getLog().info("Ignoring cleanup event because deleteEnabled is false for feed: {}", new Object[]{feedId});
            context.yield();
            return;  // ignore events if deleteEnabled is not true
        }

        // Create attributes for FlowFile
        Map<String, String> attributes = Maps.newHashMap();

        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            attributes.put((String) property.getKey(), (String) property.getValue());
        }

        attributes.put("category", context.getProperty(CATEGORY_NAME).getValue());
        attributes.put("feed", context.getProperty(FEED_NAME).getValue());

        // Create a FlowFile from the event
        FlowFile flowFile = session.create();
        flowFile = session.putAllAttributes(flowFile, attributes);

        getLog().debug("Transferring flow file to Success relationship");

        session.transfer(flowFile, REL_SUCCESS);
    }

    /**
     * Clean up resources used by this processor.
     *
     * @param context the process context
     */
    @OnUnscheduled
    public void onUnscheduled(@Nonnull final ProcessContext context) {
        // Remove listener
        getLog().debug("Unscheduled");
        getCleanupService(context).removeListener(this);
    }

    @Override
    public void triggered(@Nonnull final FeedCleanupTriggerEvent event) {
        getLog().debug("Cleanup event triggered: {}", new Object[]{event});
        queue.add(event);
    }

    /**
     * Gets the cleanup service for the specified context.
     *
     * @param context the process context
     * @return the cleanup service
     */
    @Nonnull
    private CleanupEventService getCleanupService(@Nonnull final ProcessContext context) {
        return context.getProperty(CLEANUP_SERVICE).asControllerService(CleanupEventService.class);
    }

    /**
     * Gets the metadata service for the specified context.
     *
     * @param context the process context
     * @return the metadata service
     */
    @Nonnull
    private MetadataProviderService getMetadataService(@Nonnull final ProcessContext context) {
        return context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }
}
