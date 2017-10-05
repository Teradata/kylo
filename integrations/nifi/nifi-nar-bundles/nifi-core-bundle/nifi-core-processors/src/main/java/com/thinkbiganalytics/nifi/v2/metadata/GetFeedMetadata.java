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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Adds feed metadata as {@link FlowFile} attributes.
 */
@CapabilityDescription("Adds feed metadata json as 'feedJson' flow file attribute. "
                       + "It is then possible to create new Nifi attributes which refer to "
                       + "feed metadata fields using Nifi's json expressions, "
                       + "e.g. ${feedJson:jsonPath('$.category.systemName')}. "
                       + "This processor will cache feed metadata for configurable duration "
                       + "to avoid making unnecessary calls to remote Metadata service if "
                       + "this processor is scheduled to run continuously")
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "metadata", "thinkbig"})
public class GetFeedMetadata extends AbstractNiFiProcessor {

    /**
     * Property for the feed system name
     */
    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System feed name")
        .description("The system name of this feed. The default is to have this name automatically set when the feed is created. Normally you do not need to change the default value.")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("${feed}")
        .expressionLanguageSupported(true)
        .required(true)
        .build();

    /**
     * Property for the category system name
     */
    public static final PropertyDescriptor CATEGORY_NAME = new PropertyDescriptor.Builder()
        .name("System feed category")
        .description("The category name of this feed. The default is to have this name automatically set when the feed is created. Normally you do not need to change the default value.")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("${category}")
        .expressionLanguageSupported(true)
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

    public static final PropertyDescriptor CACHE_EXPIRE_DURATION = new PropertyDescriptor.Builder()
        .name("Cache Expiry Duration")
        .description("The length of time after which this processor will request updated version of feed metadata from remote Metadata service")
        .required(true)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .expressionLanguageSupported(false)
        .defaultValue("1 secs")
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
    private static final List<PropertyDescriptor> properties = ImmutableList.of(METADATA_SERVICE, CATEGORY_NAME, FEED_NAME, CACHE_EXPIRE_DURATION);

    /**
     * List of relationships
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(REL_SUCCESS);

    private static final int CACHE_SIZE = 1;

    private LoadingCache<FeedKey, String> cachedFeed;

    private static class FeedKey {
        String categoryName;
        String feedName;

        FeedKey(String categoryName, String feedName) {
            this.categoryName = categoryName;
            this.feedName = feedName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FeedKey other = (FeedKey) o;

            if (!categoryName.equals(other.categoryName)) {
                return false;
            }
            return feedName.equals(other.feedName);
        }

        @Override
        public int hashCode() {
            int result = categoryName.hashCode();
            result = 31 * result + feedName.hashCode();
            return result;
        }
    }

    /**
     * Initializes resources required to trigger this processor.
     *
     * @param context the process context
     */
    @OnScheduled
    public void onScheduled(@Nonnull final ProcessContext context) {
        getLog().debug("Scheduled");

        TimeUnit timeUnit = TimeUnit.NANOSECONDS;
        Long nanos = context.getProperty(CACHE_EXPIRE_DURATION).asTimePeriod(timeUnit);
        cachedFeed = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterWrite(nanos, timeUnit)
            .build(
                new CacheLoader<FeedKey, String>() {
                    public String load(@Nonnull FeedKey key) {
                        return ObjectMapperSerializer.serialize(getMetadataService(context).getProvider().getFeed(key.categoryName, key.feedName));
                    }
                });
    }

    @Override
    public void onTrigger(@Nonnull final ProcessContext context, @Nonnull final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        String categoryName = context.getProperty(CATEGORY_NAME).evaluateAttributeExpressions(flowFile).getValue();
        String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        getLog().debug("Triggered for {}.{}", new Object[]{categoryName, feedName});

        String feedJson;
        try {
            feedJson = cachedFeed.get(new FeedKey(categoryName, feedName));
        } catch (Exception e) {
            getLog().error("Failure retrieving metadata for feed: {}.{}", new Object[]{categoryName, feedName}, e);
            throw new IllegalStateException("Failed to retrieve feed metadata", e);
        }

        if (feedJson == null) {
            throw new IllegalStateException(String.format("Failed to retrieve feed metadata for feed %s:%s", categoryName, feedName));
        }

        // Create attributes for FlowFile
        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("feedJson", feedJson);

        // Create a FlowFile from the event
        flowFile = session.putAllAttributes(flowFile, attributes);

        getLog().trace("Transferring flow file to Success relationship");

        session.transfer(flowFile, REL_SUCCESS);
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

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

}
