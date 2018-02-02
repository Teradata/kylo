package com.thinkbiganalytics.nifi.v2.reindex;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedsForDataHistoryReindex;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * NiFi Processor to get Kylo feeds requiring reindexing of historical data
 */
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "reindexing", "historical processing", "thinkbig", "kylo"})
@CapabilityDescription("Get Kylo feeds requiring reindexing of historical data")
@WritesAttributes({
                      @WritesAttribute(attribute = "history.reindex.feed.id", description = "ID of feed that requires reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feed.systemName", description = "System name of feed that requires reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feed.category.systemName", description = "System name of category for feed that requires reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feed.status", description = "Reindexing status for feed that requires reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feed.last.modified.utc", description = "Last modified status (reindex) for feed that requires reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feeds.total.count", description = "The total number of feeds found that require reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feeds.total.ids", description = "The ids of all feeds found that require reindexing historical data"),
                      @WritesAttribute(attribute = "history.reindex.feeds.check.time.utc", description = "The time (UTC) of check (for feeds that require reindexing historical data)")
                  })
public class GetFeedsHistoryReindex extends AbstractNiFiProcessor {

    public static final String FEED_ID_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.id";
    public static final String FEED_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.systemName";
    public static final String FEED_CATEGORY_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.category.systemName";
    public static final String FEED_STATUS_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.status";
    public static final String FEED_LAST_MODIFIED_UTC_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.last.modified.utc";
    public static final String FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY = "history.reindex.feeds.total.count";
    public static final String FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY = "history.reindex.feeds.total.ids";
    public static final String FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY = "history.reindex.feeds.check.time.utc";

    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Provider Service")
        .description("Service supplying the implementations of the various metadata providers.")
        .identifiesControllerService(MetadataProviderService.class)
        .required(true)
        .build();

    public static final Relationship REL_FOUND = new Relationship.Builder()
        .description("Found feeds requiring reindexing historical data. Each feed will result in a flow file on this relationship")
        .name("feeds_found")
        .build();

    public static final Relationship REL_NOT_FOUND = new Relationship.Builder()
        .description("No feeds found requiring reindexing historical data")
        .name("no_feeds_found")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .description("Failure to get feeds requiring reindexing historical data")
        .name("failure")
        .build();

    public static final Relationship REL_ORIGINAL = new Relationship.Builder()
        .description("The original flow file that was split into multiple flow files based on feeds found for history reindexing. "
                     + "This will only be applicable for cases where one or more feeds are found for history reindexing.")
        .name("original")
        .build();

    private static final List<PropertyDescriptor> properties = ImmutableList.of(METADATA_SERVICE);

    private static final Set<Relationship> relationships = ImmutableSet.of(REL_FOUND,
                                                                           REL_NOT_FOUND,
                                                                           REL_FAILURE,
                                                                           REL_ORIGINAL);

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            flowFile = session.create();
        }

        logger.debug("Checking for feeds requiring reindexing historical data");
        try {
            MetadataProviderService metadataProviderService = getMetadataService(context);
            if ((metadataProviderService != null) && (metadataProviderService.getProvider() != null)) {
                String dateTimeOfCheck = String.valueOf(DateTime.now(DateTimeZone.UTC));
                FeedsForDataHistoryReindex feedsForHistoryReindexing = getMetadataService(context).getProvider().getFeedsForHistoryReindexing();
                if (feedsForHistoryReindexing != null) {
                    logger.info("Found {} feeds requiring reindexing historical data", new Object[]{feedsForHistoryReindexing.getFeeds().size()});
                    if (feedsForHistoryReindexing.getFeedCount() > 0) {
                        for (Feed feedForHistoryReindexing : feedsForHistoryReindexing.getFeeds()) {
                            Map<String, String> attributes = new HashMap<>();
                            attributes.put(FEED_ID_FOR_HISTORY_REINDEX_KEY, feedForHistoryReindexing.getId());
                            attributes.put(FEED_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY, feedForHistoryReindexing.getSystemName());
                            attributes.put(FEED_CATEGORY_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY, feedForHistoryReindexing.getCategory().getSystemName());
                            attributes.put(FEED_STATUS_FOR_HISTORY_REINDEX_KEY, feedForHistoryReindexing.getCurrentHistoryReindexingStatus().getHistoryReindexingState().toString());
                            attributes.put(FEED_LAST_MODIFIED_UTC_FOR_HISTORY_REINDEX_KEY, feedForHistoryReindexing.getCurrentHistoryReindexingStatus().getLastModifiedTimestamp().toString());
                            attributes.put(FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY, String.valueOf(feedsForHistoryReindexing.getFeedCount()));
                            attributes.put(FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY, feedsForHistoryReindexing.getFeedIds().toString());
                            attributes.put(FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY, dateTimeOfCheck);
                            FlowFile feedFlowFile = session.create(flowFile); //all attributes from parent flow file copied except uuid, creates a FORK event
                            feedFlowFile = session.putAllAttributes(feedFlowFile, attributes);
                            session.transfer(feedFlowFile, REL_FOUND);
                            logger.info("Flow file created for reindexing feed's historical data: feed id {}, category name {}, feed name {}",
                                        new Object[]{FEED_ID_FOR_HISTORY_REINDEX_KEY, FEED_CATEGORY_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY, FEED_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY});
                        }
                        flowFile = session.putAttribute(flowFile, FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY, String.valueOf(feedsForHistoryReindexing.getFeedCount()));
                        flowFile = session.putAttribute(flowFile, FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY, dateTimeOfCheck);
                        session.transfer(flowFile, REL_ORIGINAL); //only for found case
                    } else {
                        flowFile = session.putAttribute(flowFile, FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY, String.valueOf(feedsForHistoryReindexing.getFeedCount())); //this will always be 0 here
                        flowFile = session.putAttribute(flowFile, FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY, feedsForHistoryReindexing.getFeedIds().toString());  //this will always be empty list here
                        flowFile = session.putAttribute(flowFile, FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY, dateTimeOfCheck);
                        session.transfer(flowFile, REL_NOT_FOUND);
                    }
                }
            } else {
                logger.error("Error checking for feeds requiring reindexing historical data. Check if Kylo services is running, and accessible from NiFi.");
                session.transfer(flowFile, REL_FAILURE);
            }
        } catch (Exception e) {
            logger.error("An exception was thrown during check for feeds requiring reindexing historical data: {}", new Object[]{e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    /* Gets the metadata service for the specified context.
     *
     * @param context the process context
     * @return the metadata service
     */
    private MetadataProviderService getMetadataService(@Nonnull final ProcessContext context) {
        return context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }
}
