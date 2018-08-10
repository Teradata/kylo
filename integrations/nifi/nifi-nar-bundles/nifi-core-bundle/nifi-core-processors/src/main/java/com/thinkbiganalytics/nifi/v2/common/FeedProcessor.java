/**
 *
 */
package com.thinkbiganalytics.nifi.v2.common;

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

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.processor.BaseProcessor;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_NAME;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * An abstract processor that can be configured with the feed canteory and name and
 * which will look up the feed's ID.
 */
public abstract class FeedProcessor extends BaseProcessor {

    /**
     * The max number of feed ID entries to cache;
     */
    private static final int DEFAULT_FEED_ID_CACHE_SIZE = 1024;

    /**
     * The attribute in the flow file containing feed ID
     */
    public static final String FEED_ID_ATTR = "feedId";

    private static final Logger log = LoggerFactory.getLogger(FeedProcessor.class);

    private Cache<String, String> feedIdCache;
    
    private transient MetadataProviderService providerService;

    @OnScheduled
    public void scheduled(ProcessContext context) {
        this.providerService = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        this.feedIdCache = CacheBuilder.newBuilder().maximumSize(DEFAULT_FEED_ID_CACHE_SIZE).build();
    }

    public FlowFile initialize(ProcessContext context, ProcessSession session, FlowFile flowFile) {
        return ensureFeedId(context, session, flowFile);
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(CommonProperties.METADATA_SERVICE);
        list.add(CommonProperties.FEED_CATEGORY);
        list.add(CommonProperties.FEED_NAME);
    }

    protected MetadataProvider getMetadataProvider() {
        return this.providerService.getProvider();
    }

    protected MetadataRecorder getMetadataRecorder() {
        return this.providerService.getRecorder();
    }

    protected FlowFile ensureFeedId(ProcessContext context, ProcessSession session, FlowFile flowFile) {
        String feedId = flowFile.getAttribute(FEED_ID_ATTR);

        if (feedId == null) {
            feedId = lookupFeedId(context, flowFile);
            return session.putAttribute(flowFile, FEED_ID_ATTR, feedId);
        } else {
            return flowFile;
        }
    }

    protected String getFeedId(ProcessContext context, FlowFile flowFile) {
        String feedId = flowFile.getAttribute(FEED_ID_ATTR);

        if (feedId == null) {
            return lookupFeedId(context, flowFile);
        } else {
            return feedId;
        }
    }

    protected void invalidFeedCache(String category, String feedName) {
        final String feedKey = category + "." + feedName;
        String feedId = feedIdCache.getIfPresent(feedKey);
        feedIdCache.invalidate(feedKey);

        if (feedId != null) {
            MetadataRecorder recorder = getMetadataRecorder();
            recorder.invalidInitializationStatus(feedId);
        }
    }

    private String lookupFeedId(ProcessContext context, FlowFile flowFile) {
        final String category = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final String feedKey = category + "." + feedName;
        
        try {
            log.debug("Resolving ID for feed {}/{} from cache", category, feedName);
            return this.feedIdCache.get(feedKey, supplyMetadataFeedId(category, feedName));
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause());
            log.error("Failed to retrieve ID for feed {}/{}", category, feedName, e.getCause());
            throw new ProcessException("Failed to retrieve feed ID", e);
        }
    }
    
    private Callable<String> supplyMetadataFeedId(String category, String feedName) {
        return () -> {
            log.debug("Resolving ID for feed {}/{} from metadata server", category, feedName);
            final String feedId = getMetadataProvider().getFeedId(category, feedName);
            
            if (feedId != null) {
                log.debug("Resolving id {} for feed {}/{}", feedId, category, feedName);
                return feedId;
            } else {
                log.warn("ID for feed {}/{} could not be located", category, feedName);
                throw new FeedIdNotFoundException(category, feedName);
            }
        };
    }

}
