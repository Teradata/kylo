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

import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedDataHistoryReindexParams;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingState;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingStatus;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * NiFi Processor to update history reindexing status of a Kylo feed
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "reindexing", "historical processing", "thinkbig", "kylo"})
@CapabilityDescription("Update history reindexing status of a Kylo feed")
@ReadsAttributes({
                     @ReadsAttribute(attribute = "history.reindex.feed.id", description = "ID of feed that requires reindexing historical data"),
                     @ReadsAttribute(attribute = "history.reindex.feed.status.for.update", description = "New reindexing status to set for feed")
                 })
@WritesAttributes({
                      @WritesAttribute(attribute = "history.reindex.feed.updated.feed.info", description = "Info about feed for which history reindexing status was updated"),
                      @WritesAttribute(attribute = "history.reindex.feed.updated.status", description = "Updated history reindexing status for feed"),
                      @WritesAttribute(attribute = "history.reindex.feed.updated.time.utc", description = "Time when history reindexing status for feed was updated"),
                      @WritesAttribute(attribute = "history.reindex.feed.updated.index.columns.string", description = "String of comma separated columns for indexing")
                  })
public class UpdateFeedHistoryReindex extends AbstractNiFiProcessor {

    public static final String UPDATED_FEED_INFO_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.updated.feed.info";
    public static final String UPDATED_FEED_STATUS_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.updated.status";
    public static final String UPDATED_TIME_UTC_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.updated.time.utc";
    public static final String UPDATED_INDEX_COLUMNS_STRING_FOR_HISTORY_REINDEX_KEY = "history.reindex.feed.updated.index.columns.string";

    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Provider Service")
        .description("Service supplying the implementations of the various metadata providers.")
        .identifiesControllerService(MetadataProviderService.class)
        .required(true)
        .build();

    public static final PropertyDescriptor FEED_ID = new PropertyDescriptor.Builder()
        .name("Feed Id")
        .description("Kylo's feed id for which status needs to be updated")
        .defaultValue("${history.reindex.feed.id}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor FEED_REINDEX_STATUS = new PropertyDescriptor.Builder()
        .name("Reindexing status")
        .description("Updated reindexing status for feed")
        .defaultValue("${history.reindex.feed.status.for.update}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .description("Successfully updated feed's history reindexing status")
        .name("success")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .description("Failure in updating feed's history reindexing status")
        .name("failure")
        .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(METADATA_SERVICE);
        properties.add(FEED_ID);
        properties.add(FEED_REINDEX_STATUS);
        this.properties = Collections.unmodifiableList(properties);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            logger.warn("Update feed history reindex status processor called without an in-coming flow file. "
                        + "Ensure that this is the intended usage.");
            flowFile = session.create();
        }

        final MetadataProviderService metadataProviderService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        final String feedId = context.getProperty(FEED_ID).evaluateAttributeExpressions(flowFile).getValue();
        final String historyReindexingStateAsString = context.getProperty(FEED_REINDEX_STATUS).evaluateAttributeExpressions(flowFile).getValue();

        logger.debug("Updating history reindex status for feed with id {}. New status for update is {}...", new Object[]{feedId, historyReindexingStateAsString});

        if (feedId != null && metadataProviderService != null && metadataProviderService.getRecorder() != null) {
            try {
                FeedDataHistoryReindexParams
                    feedDataHistoryReindexParams =
                    metadataProviderService.getRecorder()
                        .updateFeedHistoryReindexing(feedId,
                                                     new HistoryReindexingStatus(HistoryReindexingState.valueOf(historyReindexingStateAsString)));

                if (feedDataHistoryReindexParams == null) {
                    logger.error("Error updating history reindex status to {} for feed with id {}", new Object[]{feedId, historyReindexingStateAsString});
                    session.transfer(flowFile, REL_FAILURE);
                } else {
                    flowFile = session.putAttribute(flowFile, UPDATED_FEED_INFO_FOR_HISTORY_REINDEX_KEY, getFeedInfo(feedDataHistoryReindexParams));
                    flowFile =
                        session.putAttribute(flowFile, UPDATED_FEED_STATUS_FOR_HISTORY_REINDEX_KEY, feedDataHistoryReindexParams.getHistoryReindexingStatus().getHistoryReindexingState().toString());
                    flowFile =
                        session.putAttribute(flowFile, UPDATED_TIME_UTC_FOR_HISTORY_REINDEX_KEY, feedDataHistoryReindexParams.getHistoryReindexingStatus().getLastModifiedTimestamp().toString());
                    flowFile = session.putAttribute(flowFile, UPDATED_INDEX_COLUMNS_STRING_FOR_HISTORY_REINDEX_KEY, feedDataHistoryReindexParams.getCommaSeparatedColumnsForIndexing());
                    logger.info("Updated reindex history status to {} for feed with id {}", new Object[]{
                        feedDataHistoryReindexParams.getHistoryReindexingStatus().getHistoryReindexingState().toString(),
                        feedDataHistoryReindexParams.getFeedId()});
                    session.transfer(flowFile, REL_SUCCESS);
                }
            } catch (Exception e) {
                logger.error("An exception was thrown during updating history reindex status to {} for feed id {}: {}", new Object[]{
                    feedId,
                    historyReindexingStateAsString,
                    e
                });
                session.transfer(flowFile, REL_FAILURE);
            }
        } else {
            logger.error("One or more of {feed id, metadata service, metadata service recorder} is null for updating history reindex status operation");
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    private String getFeedInfo(FeedDataHistoryReindexParams feedDataHistoryReindexParams) {
        return "feed id: "
               + feedDataHistoryReindexParams.getFeedId()
               + ", feed name: "
               + feedDataHistoryReindexParams.getCategorySystemName()
               + "."
               + feedDataHistoryReindexParams.getFeedSystemName();
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final List<ValidationResult> results = new ArrayList<>();
        final String historyReindexingStateAsString = validationContext.getProperty(FEED_REINDEX_STATUS).evaluateAttributeExpressions().getValue();
        final String historyReindexingStateAsExpression = validationContext.getProperty(FEED_REINDEX_STATUS).getValue();
        if (!(((isValidHistoryReindexingState(historyReindexingStateAsString)) || (historyReindexingStateAsExpression.equals("${history.reindex.feed.status.for.update}"))))) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("Updated state is not valid. Valid states are " + StringUtils.join(HistoryReindexingState.values(), ",") + ". Also, this can be passed via "
                                         + "the attribute history.reindex.feed.status.for.update in flow file and referenced via Expression language here as ${history.reindex.feed.status.for.update}")
                            .build());
        }

        return results;
    }

    private boolean isValidHistoryReindexingState(String historyReindexingStateAsString) {
        for (HistoryReindexingState state : HistoryReindexingState.values()) {
            if (state.name().equals(historyReindexingStateAsString)) {
                return true;
            }
        }
        return false;
    }
}
