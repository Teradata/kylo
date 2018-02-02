package com.thinkbiganalytics.nifi.v2.core.metadata;

import com.fasterxml.jackson.core.type.TypeReference;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.nifi.core.api.metadata.ActiveWaterMarksCancelledException;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedDataHistoryReindexParams;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingStatus;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.core.api.metadata.WaterMarkActiveException;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.Nonnull;

public class MetadataClientRecorder extends AbstractControllerService implements MetadataRecorder {

    private static final Logger log = LoggerFactory.getLogger(MetadataClientRecorder.class);

    private static final String CURRENT_WATER_MARKS_ATTR = "activeWaterMarks";
    private static final TypeReference<NavigableMap<String, WaterMarkParam>> WM_MAP_TYPE = new TypeReference<NavigableMap<String, WaterMarkParam>>() { };
    private static final ObjectReader WATER_MARKS_READER = new ObjectMapper().reader().forType(WM_MAP_TYPE);
    private static final ObjectWriter WATER_MARKS_WRITER = new ObjectMapper().writer().forType(WM_MAP_TYPE);

    private MetadataClient client;
    private NavigableMap<String, Long> activeWaterMarks = new ConcurrentSkipListMap<>();
    private Map<String, InitializationStatus> activeInitStatuses = Collections.synchronizedMap(new HashMap<>());

    /**
     * constructor creates a MetadataClientRecorder with the default URI constant
     */
    public MetadataClientRecorder() {
        this(URI.create("http://localhost:8420/api/v1/metadata"));
    }

    /**
     * constructor creates a MetadataClientRecorder with the URI provided
     *
     * @param baseUri the REST endpoint of the Metadata recorder
     */
    public MetadataClientRecorder(URI baseUri) {
        this(new MetadataClient(baseUri));
    }

    /**
     * constructor creates a MetadataClientProvider with the required {@link MetadataClientRecorder}
     *
     * @param client the MetadataClient will be used to connect with the Metadata store
     */
    public MetadataClientRecorder(MetadataClient client) {
        this.client = client;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#loadWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile loadWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String parameterName, String initialValue) throws WaterMarkActiveException {
        long timestamp = recordActiveWaterMark(feedId, waterMarkName);

        String value = getHighWaterMarkValue(feedId, waterMarkName).orElse(initialValue);
        FlowFile resultFF = addToCurrentWaterMarksAttr(session, ff, waterMarkName, parameterName, timestamp);
        resultFF = session.putAttribute(resultFF, parameterName, value);
        return session.putAttribute(resultFF, originalValueParameterName(parameterName), value);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#cancelWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public boolean cancelWaterMark(String feedId, String waterMarkName) {
        return cancelActiveWaterMark(feedId, waterMarkName);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#cancelAndLoadWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile cancelAndLoadWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String parameterName, String initialValue) throws WaterMarkActiveException {
        cancelActiveWaterMark(feedId, waterMarkName);
        return loadWaterMark(session, ff, feedId, waterMarkName, parameterName, initialValue);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#recordWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String, java.io.Serializable)
     */
    @Override
    public FlowFile recordWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String parameterName, String newValue) {
        Map<String, WaterMarkParam> actives = getCurrentWaterMarksAttr(ff);

        if (actives.containsKey(waterMarkName)) {
            return session.putAttribute(ff, parameterName, newValue);
        } else {
            throw new IllegalStateException("No active high-water mark named \"" + waterMarkName + "\"");
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#commitWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile commitWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName) {
        FlowFile resultFF = ff;
        Map<String, WaterMarkParam> ffWaterMarks = getCurrentWaterMarksAttr(ff);
        WaterMarkParam param = ffWaterMarks.get(waterMarkName);
        Long activeTimestamp = getActiveWaterMarkTimestamp(feedId, waterMarkName);

        if (param == null) {
            log.error("Received request to commit a water mark that does not exist in the flowfile: {}", waterMarkName);
            return resultFF;
        } else if (activeTimestamp == null) {
            log.warn("Received request to commit a water mark that is not active: {}", waterMarkName);
            return resultFF;
        } else if (param.timestamp != activeTimestamp) {
            // If the water mark timestamp does not match the one recorded as an active water mark this means
            // this flowfile's water mark has been canceled and another flow file should be considered the active one.
            // So this water mark value has been superseded and its value should not be committed and a canceled exception thrown.
            log.info("Received request to commit a water mark version that is no longer active: {}/{}", waterMarkName, param.timestamp);
            throw new ActiveWaterMarksCancelledException(feedId, waterMarkName);
        }

        try {
            String value = resultFF.getAttribute(param.name);
            updateHighWaterMarkValue(feedId, waterMarkName, value);
        } finally {
            releaseActiveWaterMark(feedId, waterMarkName, activeTimestamp);
            resultFF = removeFromCurrentWaterMarksAttr(session, resultFF, waterMarkName, param.name);
        }

        return resultFF;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#commitAllWaterMarks(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String)
     */
    @Override
    public FlowFile commitAllWaterMarks(ProcessSession session, FlowFile ff, String feedId) {
        FlowFile resultFF = ff;
        Set<String> cancelledWaterMarks = new HashSet<>();

        // TODO do more efficiently
        for (String waterMarkName : new HashSet<String>(getCurrentWaterMarksAttr(ff).keySet())) {
            try {
                resultFF = commitWaterMark(session, resultFF, feedId, waterMarkName);
            } catch (ActiveWaterMarksCancelledException e) {
                cancelledWaterMarks.addAll(e.getWaterMarkNames());
            }
        }

        if (cancelledWaterMarks.size() > 0) {
            throw new ActiveWaterMarksCancelledException(feedId, cancelledWaterMarks);
        } else {
            return resultFF;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#releaseWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile releaseWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName) {
        FlowFile resultFF = ff;
        Map<String, WaterMarkParam> ffWaterMarks = getCurrentWaterMarksAttr(ff);
        WaterMarkParam param = ffWaterMarks.get(waterMarkName);

        try {
            if (param != null) {
                // Update the flowfile with the modified set of active water marks.
                removeFromCurrentWaterMarksAttr(session, resultFF, waterMarkName, param.name);
                resetWaterMarkParam(session, resultFF, feedId, waterMarkName, param.name);
            } else {
                log.warn("Received request to release a water mark not found in the flow file: {}", waterMarkName);
            }
        } finally {
            // Even if water mark resetting fails we should always release the water mark.
            Long activeTimestamp = getActiveWaterMarkTimestamp(feedId, waterMarkName);

            if (activeTimestamp != null) {
                if (param == null || param.timestamp == activeTimestamp) {
                    releaseActiveWaterMark(feedId, waterMarkName, activeTimestamp);
                } else if (param.timestamp != activeTimestamp) {
                    // If the water mark timestamp does not match the one recorded as an active water mark this means
                    // this flowfile's water mark has been canceled and another flow file should be considered the active one.
                    // In this case this water mark value has been superseded and no release should occur.
                    log.info("Received request to release a water mark version that is no longer active: {}", waterMarkName);
                }
            } else {
                // The water mark is not one recognize as an active one.
                log.warn("Received request to release a non-active water mark: {}", waterMarkName);
            }
        }

        return resultFF;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#releaseAllWaterMarks(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String)
     */
    @Override
    public FlowFile releaseAllWaterMarks(ProcessSession session, FlowFile ff, String feedId) {
        FlowFile resultFF = ff;

        for (String waterMarkName : new HashSet<String>(getCurrentWaterMarksAttr(ff).keySet())) {
            resultFF = releaseWaterMark(session, resultFF, feedId, waterMarkName);
        }

        return resultFF;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#getFeedInitializationStatus(java.lang.String)
     */
    @Override
    public Optional<InitializationStatus> getInitializationStatus(String feedId) {
        // Defer to the local active state first
        Optional<InitializationStatus> option = Optional.ofNullable(this.activeInitStatuses.get(feedId));
        return option.isPresent() ? option : Optional.ofNullable(this.client.getCurrentInitStatus(feedId));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#startFeedInitialization(java.lang.String)
     */
    @Override
    public InitializationStatus startFeedInitialization(String feedId) {
        InitializationStatus status = new InitializationStatus(InitializationStatus.State.IN_PROGRESS);
        this.activeInitStatuses.put(feedId, status);
        return status;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#completeFeedInitialization(java.lang.String)
     */
    @Override
    public InitializationStatus completeFeedInitialization(String feedId) {
        InitializationStatus status = new InitializationStatus(InitializationStatus.State.SUCCESS);
        try {
            this.client.updateCurrentInitStatus(feedId, status);
            this.activeInitStatuses.remove(feedId);
            return status;
        } catch (Exception e) {
            log.error("Failed to update metadata with feed initialization completion status: {},  feed: {}", status.getState(), feedId, e);
            throw new ProcessException("Failed to update metadata with feed initialization completion status: " + status + ",  feed: " + feedId, e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#failFeedInitialization(java.lang.String)
     */
    @Override
    public InitializationStatus failFeedInitialization(String feedId) {
        InitializationStatus status = new InitializationStatus(InitializationStatus.State.FAILED);
        try {
            this.client.updateCurrentInitStatus(feedId, status);
            this.activeInitStatuses.remove(feedId);
            return status;
        } catch (Exception e) {
            log.error("Failed to update feed initialization completion status: {},  feed: {}", status.getState(), feedId);
            this.activeInitStatuses.put(feedId, status);
            return status;
        }
    }

    @Override
    public void updateFeedStatus(ProcessSession session, FlowFile ff, String statusMsg) {
        // TODO Auto-generated method stub

    }

    @Override
    public FeedDataHistoryReindexParams updateFeedHistoryReindexing(@Nonnull String feedId, @Nonnull HistoryReindexingStatus historyReindexingStatus) {
            return (this.client.updateFeedHistoryReindexing(feedId, historyReindexingStatus));
    }

    private String toWaterMarksKey(String feedId) {
        return feedId + ".~";
    }

    private String toWaterMarkKey(String feedId, String waterMarkName) {
        return feedId + "." + waterMarkName;
    }

    private Long getActiveWaterMarkTimestamp(String feedId, String waterMarkName) {
        return this.activeWaterMarks.get(toWaterMarkKey(feedId, waterMarkName));
    }

    private Map<String, Long> getActiveWaterMarks(String feedId, String waterMarkName) {
        return this.activeWaterMarks.subMap(feedId, toWaterMarksKey(feedId));
    }

    private boolean cancelActiveWaterMark(String feedId, String waterMarkName) {
        return this.activeWaterMarks.remove(toWaterMarkKey(feedId, waterMarkName)) == null;
    }

    private long recordActiveWaterMark(String feedId, String waterMarkName) throws WaterMarkActiveException {
        long newTimestamp = System.currentTimeMillis();
        long timestamp = this.activeWaterMarks.computeIfAbsent(toWaterMarkKey(feedId, waterMarkName), k -> newTimestamp);

        if (timestamp != newTimestamp) {
            throw new WaterMarkActiveException(waterMarkName);
        } else {
            return newTimestamp;
        }
    }

    private boolean releaseActiveWaterMark(String feedId, String waterMarkName, long expectedTimestamp) {
        long timestamp = this.activeWaterMarks.remove(toWaterMarkKey(feedId, waterMarkName));
        return expectedTimestamp == timestamp;
    }

    private NavigableMap<String, WaterMarkParam> getCurrentWaterMarksAttr(FlowFile ff) {
        try {
            String activeStr = ff.getAttribute(CURRENT_WATER_MARKS_ATTR);

            if (activeStr == null) {
                return new TreeMap<>();
            } else {
                return WATER_MARKS_READER.readValue(activeStr);
            }
        } catch (Exception e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }

    private FlowFile setCurrentWaterMarksAttr(ProcessSession session, FlowFile ff, NavigableMap<String, WaterMarkParam> actives) {
        try {
            return session.putAttribute(ff, CURRENT_WATER_MARKS_ATTR, WATER_MARKS_WRITER.writeValueAsString(actives));
        } catch (Exception e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }

    private FlowFile resetWaterMarkParam(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String paramName) {
        // Update the flowfile with the original value for the water mark parameter.
        String value = getHighWaterMarkValue(feedId, waterMarkName).orElse(ff.getAttribute(originalValueParameterName(paramName)));

        if (value != null) {
            return session.putAttribute(ff, paramName, value);
        } else {
            log.error("Failed to reset water mark - original value not found in flowfile for water mark: {}", waterMarkName);
            throw new IllegalStateException("Failed to reset water mark - original value not found in flowfile for water mark: " + waterMarkName);
        }
    }

    private FlowFile addToCurrentWaterMarksAttr(ProcessSession session, FlowFile ff, String waterMarkName, String parameterName, long timestamp) {
        NavigableMap<String, WaterMarkParam> actives = getCurrentWaterMarksAttr(ff);

        actives.put(waterMarkName, new WaterMarkParam(timestamp, parameterName));
        return setCurrentWaterMarksAttr(session, ff, actives);
    }

    private FlowFile removeFromCurrentWaterMarksAttr(ProcessSession session, FlowFile ff, String waterMarkName, String parameterName) {
        NavigableMap<String, WaterMarkParam> actives = getCurrentWaterMarksAttr(ff);

        actives.remove(waterMarkName);
        return setCurrentWaterMarksAttr(session, ff, actives);
    }

    private Optional<String> getHighWaterMarkValue(String feedId, String waterMarkName) {
        return this.client.getHighWaterMarkValue(feedId, waterMarkName);
    }

    private void updateHighWaterMarkValue(String feedId, String waterMarkName, String value) {
        this.client.updateHighWaterMarkValue(feedId, waterMarkName, value);
    }

    private String originalValueParameterName(String parameterName) {
        return parameterName + ".original";
    }


    public static class WaterMarkParam {
        private long timestamp;
        private String name;

        public WaterMarkParam() {
            super();
        }

        public WaterMarkParam(long timestamp, String value) {
            this.timestamp = timestamp;
            this.name = value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
