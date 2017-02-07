package com.thinkbiganalytics.nifi.provenance.reporting;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventCollector;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileMapDbCache;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnConfigurationRestored;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.state.Scope;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.components.state.StateMap;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventRepository;
import org.apache.nifi.reporting.AbstractReportingTask;
import org.apache.nifi.reporting.EventAccess;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.reporting.ReportingContext;
import org.apache.nifi.reporting.ReportingInitializationContext;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
@Tags({"reporting", "kylo", "provenance"})
@CapabilityDescription("Publishes Provenance Events to the JMS queues for Kylo")
public class KyloProvenanceEventReportingTask extends AbstractReportingTask {

    public static final String LAST_EVENT_ID_KEY = "kyloLastEventId";

    private static enum LAST_EVENT_ID_NOT_FOUND_OPTION {ZERO, MAX_EVENT_ID, KYLO}

    private static enum INITIAL_EVENT_ID_OPTION {LAST_EVENT_ID, MAX_EVENT_ID, KYLO}


    PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("Think Big metadata service")
        .required(true)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    protected static final PropertyDescriptor MAX_BATCH_FEED_EVENTS_PER_SECOND = new PropertyDescriptor.Builder()
        .name("Max batch feed events per second")
        .description("The maximum number of events/second for a given feed allowed to go through to Kylo.  This is used to safeguard Kylo against a feed that starts acting like a stream")
        .required(false)
        .defaultValue("10")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    protected static final PropertyDescriptor JMS_EVENT_GROUP_SIZE = new PropertyDescriptor.Builder()
        .name("JMS event group size")
        .description("The size of grouped events sent over to Kylo.  This should be less than the Processing Batch Size")
        .defaultValue("50")
        .required(false)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    protected static final PropertyDescriptor PROCESSING_BATCH_SIZE = new PropertyDescriptor.Builder()
        .name("Processing batch size")
        .description(
            "The maximum number of events to process in a given interval.  If there are more events than this number to process in a given run of this reporting task it will partition the list and process the events in batches of this size to increase throughput to Kylo.")
        .defaultValue("500")
        .required(false)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor REBUILD_CACHE_ON_RESTART = new PropertyDescriptor.Builder()
        .name("Rebuild cache on restart")
        .description(
            "Should the cache of the flows be rebuilt every time the Reporting task is restarted?  By default the system will keep the cache up to date; however, setting this to true will force the cache to be rebuilt upon restarting the reporting task. ")
        .required(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue("false")
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor LAST_EVENT_ID_NOT_FOUND_VALUE = new PropertyDescriptor.Builder()
        .name("Last event id not found value")
        .description(("If there is no minimum value to start the range query from (i.e. if this reporting task has never run before in NiFi) what should be the initial value?\""
                      + "\nZERO: this will get all events starting at 0 to the latest event id."
                      + "\nMAX_EVENT_ID: this is set it to the max provenance event.  "
                      + "\nKYLO: this will query Kylo's record of the Max Event Id. "
                      + ""))
        .allowableValues(LAST_EVENT_ID_NOT_FOUND_OPTION.values())
        .required(true)
        .defaultValue(LAST_EVENT_ID_NOT_FOUND_OPTION.KYLO.toString())
        .build();

    public static final PropertyDescriptor INITIAL_EVENT_ID_VALUE = new PropertyDescriptor.Builder()
        .name("Initial event id value")
        .description("Upon starting the Reporting task what value should be used as the minimum value in the range of provenance events this task should query? "
                     + "\nLAST_EVENT_ID: will use the last event successfully processed from this task.  This is the default setting"
                     + "\nMAX_EVENT_ID: will start processing every event > the Max event id in provenance.  This value is evaluated each time this reporting task is stopped and restarted.  You can use this to reset provenance events being sent to Kylo.  This is not the ideal behavior so you may loose provenance reporting.  Use this with caution."
                     + "\nKYLO:  will query Kylo's record of the Max Event Id. ")
        .allowableValues(INITIAL_EVENT_ID_OPTION.values())
        .required(true)
        .defaultValue(INITIAL_EVENT_ID_OPTION.LAST_EVENT_ID.toString())
        .build();


    private MetadataProviderService metadataProviderService;

    /**
     * Flag to indicate if the task is running and processing to prevent multiple threads from executing it
     */
    private AtomicBoolean processing = new AtomicBoolean(false);

    /**
     * Flag to indicate the system has started and it is loading any data from the persisted cache
     */
    private AtomicBoolean initializing = new AtomicBoolean(false);

    private boolean initializationError = false;

    /**
     * The Id used to sync with the Kylo Metadata to build the cache of NiFi processorIds in helping to determine the Flows Feed and Flows Failure Processors
     */
    private String nifiFlowSyncId = null;

    /**
     * Pointer to the NiFi StateManager used to store the lastEventId processed
     */
    private StateManager stateManager;


    /**
     * Listener when JMS successfully posts its events to the queue.
     * Used to update the StateManager storage value to ensure Kylo processes all events that have been successfully sent to the queue
     */
    private KyloReportingTaskJmsListeners.KyloReportingTaskBatchJmsListener batchJmsListener;

    /**
     * Listener when JMS successfully posts its events to the queue.
     * Used to update the StateManager storage value to ensure Kylo processes all events that have been successfully sent to the queue
     */
    private KyloReportingTaskJmsListeners.KyloReportingTaskStatsJmsListener statsJmsListener;

    /**
     * Store the value of the ReportingTask that indiciates the failsafe in case a flow starts processing a lot of events very quick
     */
    private Integer maxBatchFeedJobEventsPerSecond;

    /**
     * Events are sent over JMS and partitioned into smaller batches.
     */
    private Integer jmsEventGroupSize;


    /**
     * global log message
     */
    private String currentProcessingMessage = "";

    /**
     * reference the last processing maxEventId for logging purposes
     */
    private Long previousMax = 0L;

    /**
     * value from REBUILD_CACHE_ON_RESTART
     */
    private boolean rebuildNiFiFlowCacheOnRestart = false;

    /**
     * value from PROCESSING_BATCH_SIZE
     */
    private Integer processingBatchSize;

    /**
     * value from LAST_EVENT_ID_NOT_FOUND_VALUE
     */
    private LAST_EVENT_ID_NOT_FOUND_OPTION lastEventIdNotFoundValue;

    private boolean lastEventIdInitialized = false;

    /**
     * value from INITIAL_EVENT_ID_VALUE
     */
    private INITIAL_EVENT_ID_OPTION initialEventIdValue;

    /**
     * store the initialId that will be used to query the range of events,
     * This is reset each time the task is scheduled
     */
    private Long initialId;

    /**
     * track nifiQueryTime for provenance lookup
     */
    private Long nifiQueryTime = 0L;

    private NodeIdStrategy nodeIdStrategy;


    public KyloProvenanceEventReportingTask() {
        super();
    }


    /**
     * count the number of retry attempts when getting the flowfileMapDB Cache
     */
    private int initializeFlowFilesRetryAttempts = 0;


    @Override
    public void init(final ReportingInitializationContext context) {
        getLogger().info("init of KyloReportingTask");

    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(METADATA_SERVICE);
        properties.add(MAX_BATCH_FEED_EVENTS_PER_SECOND);
        properties.add(JMS_EVENT_GROUP_SIZE);
        properties.add(REBUILD_CACHE_ON_RESTART);
        properties.add(LAST_EVENT_ID_NOT_FOUND_VALUE);
        properties.add(INITIAL_EVENT_ID_VALUE);
        properties.add(PROCESSING_BATCH_SIZE);
        return properties;
    }


    @OnScheduled
    public void setup(final ConfigurationContext context) throws IOException, InitializationException {
        getLogger().info("OnScheduled of KyloReportingTask");
        loadSpring(true);
        this.metadataProviderService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        this.maxBatchFeedJobEventsPerSecond = context.getProperty(MAX_BATCH_FEED_EVENTS_PER_SECOND).asInteger();
        this.jmsEventGroupSize = context.getProperty(JMS_EVENT_GROUP_SIZE).asInteger();
        getProvenanceEventCollector().setMaxBatchFeedJobEventsPerSecond(this.maxBatchFeedJobEventsPerSecond);
        getProvenanceEventCollector().setJmsEventGroupSize(this.jmsEventGroupSize);
        Boolean rebuildOnRestart = context.getProperty(REBUILD_CACHE_ON_RESTART).asBoolean();

        this.processingBatchSize = context.getProperty(PROCESSING_BATCH_SIZE).asInteger();
        this.lastEventIdNotFoundValue = LAST_EVENT_ID_NOT_FOUND_OPTION.valueOf(context.getProperty(LAST_EVENT_ID_NOT_FOUND_VALUE).getValue());
        this.initialEventIdValue = INITIAL_EVENT_ID_OPTION.valueOf(context.getProperty(INITIAL_EVENT_ID_VALUE).getValue());

        //reset the initial id to null for the ability to be reset
        initialId = null;

        if (rebuildOnRestart != null) {
            this.rebuildNiFiFlowCacheOnRestart = rebuildOnRestart;
        }
        if (this.rebuildNiFiFlowCacheOnRestart && StringUtils.isNotBlank(nifiFlowSyncId)) {
            nifiFlowSyncId = null;
        }
    }

    @OnStopped
    public void onStopped(ConfigurationContext configurationContext) {
        abortProcessing();
    }


    /**
     * this.getIdentifier().toLowerCase().contains("mock")
     * When shutting down the ActiveFlowFile information is persisted to Disk
     *
     *
     */
    @OnShutdown
    public final void onShutdown(ConfigurationContext configurationContext) {
        getLogger().info("onShutdown: Attempting to persist any active flow files to disk");
        abortProcessing();
        try {
            //persist running flowfile metadata to disk
            int persistedRootFlowFiles = getFlowFileMapDbCache().persistFlowFiles();
            getLogger().info("onShutdown: Finished persisting {} root flow files to disk ", new Object[]{persistedRootFlowFiles});
        } catch (Exception e) {
            //ok to swallow exception here.  this is called when NiFi is shutting down
        }
    }


    /**
     * When NiFi comes up load any ActiveFlowFiles that have been persisted to disk
     *
     * Note on startup Nifi will run through this method twice, first using a Mock reporting task, then then later using the real one.
     * The Mock reporting task wont have any spring configuration
     */
    @OnConfigurationRestored
    public final void onConfigurationRestored() {
        if (initializing.compareAndSet(false, true)) {
            try {
                getLogger().info("onConfigurationRestored: Attempting to load any persisted files from disk into the Guava Cache");

                loadSpring(true);
                //rebuild mem flowfile metadata from disk
                initializeFlowFilesFromMapDbCache();
            } catch (Exception e) {
                getLogger().warn(
                    "Error attempting to restore FlowFile MapDB cache in onConfigurationRestored with message: {}.  The Reporting Task will attempt to initialize this again at the start of the first trigger.",
                    new Object[]{e.getMessage()});
                initializationError = true;
            } finally {
                initializing.set(false);
            }
        }
    }

    /**
     * attempt to load the data from disk into the Guava Cache
     */
    private void initializeFlowFilesFromMapDbCache() {
        int loadedRootFlowFiles = getFlowFileMapDbCache().loadGuavaCache();
        getLogger().info("initializeFlowFilesFromMapDbCache: Finished loading {} persisted files from disk into the Guava Cache", new Object[]{loadedRootFlowFiles});
    }


    /**
     * Ensures the flow files stored in the cache from the last time NiFi was shut down are loaded
     */
    private void ensureInitializeFlowFileMapDbCache() {
        if (initializationError) {
            getLogger().info("Errors was found initializing the Flow files... attempting to resolve now");
            initializing.set(true);
            //only attempt to retry 3 times
            boolean retry = initializeFlowFilesRetryAttempts < 3;
            if (retry) {
                try {
                    initializeFlowFilesFromMapDbCache();
                } catch (Exception e) {
                    initializeFlowFilesRetryAttempts++;
                    if (initializeFlowFilesRetryAttempts < 3) {
                        getLogger().error("Retry to get mapDbCache  with attempt # {}", new Object[]{initializeFlowFilesRetryAttempts});
                        //wait
                        try {
                            Thread.sleep(300L);
                        } catch (InterruptedException var10) {

                        }
                        //retry
                        ensureInitializeFlowFileMapDbCache();
                    } else {
                        getLogger().error("ERROR attempting to initialize the FlowFile MapDB cache.  Any events running midstream before NiFi was restarted may not be finished in Kylo {} ",
                                          new Object[]{e.getMessage()}, e);
                    }
                } finally {
                    initializationError = false;
                    initializing.set(false);
                }
            }
        }
    }


    /**
     * ensure Spring is loaded and Beans are autowired correctly.
     * @param logError true if the error accessing spring should be logged
     */
    private void loadSpring(boolean logError) {
        try {
            SpringApplicationContext.getInstance().initializeSpring("classpath:provenance-application-context.xml");
        } catch (BeansException | IllegalStateException e) {
            if (logError) {
                getLogger().error("Failed to load spring configurations", e);
            }
        }
    }


    /**
     * The Service provider that will talk to the Kylo Metadata via REST used to get/ensure the NiFiFlowCache is up to date
     * @return the KyloFlowProvider gaining access to Kylo managed feed information
     */
    private KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
        KyloNiFiFlowProvider kyloNiFiFlowProvider = metadataProviderService.getKyloNiFiFlowProvider();

        if (kyloNiFiFlowProvider == null) {
            getLogger().error("ERROR unable to get the KyloNiFiFlowProvider service from the metadataProviderService!");
        }
        return kyloNiFiFlowProvider;
    }

    /**
     * Gets the last Event that was saved in the StateManager the LastEventId is saved via a JMS Listener when the event is sent to JMS a callback is executed and then it updates the StateManager via
     * this method
     * @param stateManager
     * @return
     */
    protected long getLastEventId(StateManager stateManager) {
        try {
            if (stateManager == null) {
                getLogger().warn("Failed to retrieve the last event id from the "
                                 + "state manager.  State Manager is null.   Returning 0");

                return -1L;
            }
            final StateMap stateMap = stateManager.getState(Scope.LOCAL);
            final String lastEventIdStr = stateMap.get(LAST_EVENT_ID_KEY);
            final long lastEventId = lastEventIdStr != null ? Long.parseLong(lastEventIdStr) : -1L;
            return lastEventId;
        } catch (final IOException ioe) {
            getLogger().warn("Failed to retrieve the last event id from the "
                             + "state manager.", ioe);
            return -1L;
        }
        //get it from the last event that was sent to JMS
    }


    /**
     * Store the {@code eventId} that has been processed in the {@code stateManager} to indicate that it has been processed
     *
     * @param stateManager the state manager to write to
     * @param eventId      the event Id to store
     */
    private void setLastEventId(StateManager stateManager, long eventId) throws IOException {
        final StateMap stateMap = stateManager.getState(Scope.LOCAL);
        final Map<String, String> statePropertyMap = new HashMap<>(stateMap.toMap());
        statePropertyMap.put(LAST_EVENT_ID_KEY, Long.toString(eventId));
        stateManager.setState(statePropertyMap, Scope.LOCAL);
        getLogger().debug("KyloReportingTask EventId Info: Setting the Last Event Id to be {} ", new Object[]{eventId});
    }

    /**
     * Set the eventId using the saved StateManager
     *
     * @param eventId the eventId to save
     */
    protected void setLastEventId(long eventId) throws IOException {
        if (eventId > 0) {
            setLastEventId(stateManager, eventId);
        }
    }

    /**
     * Call out to Kylo to sync the Flow Cache in the reporting task with that in Kylo. This will ensure prior to running through Provenance Events that the processorIds are in sync with the latest
     * flow data in NiFi
     */
    private void updateNifiFlowCache() {
        try {
            getLogger().debug("updateNifiFlowCache starting with syncId of {} ", new Object[]{nifiFlowSyncId});
            NiFiFlowCacheSync updates = getKyloNiFiFlowProvider().getNiFiFlowUpdates(nifiFlowSyncId);
            nifiFlowSyncId = updates.getSyncId();
            getProvenanceFeedLookup().updateFlowCache(updates);
            getLogger().debug("Finished updateNifiFlowCache with syncId: {}, mapSize: {} ", new Object[]{nifiFlowSyncId, getProvenanceFeedLookup().getProcessorIdMapSize()});
        } catch (Exception e) {
            abortProcessing();
            throw e;
        }
    }


    /**
     * Aborts processing and resets the {@code processing} flag
     */
    private void abortProcessing() {
        if (processing.compareAndSet(true, false)) {
            getLogger().info("Reporting Task Aborted.  Last Event Recorded was: {} ", new Object[]{getLastEventId(stateManager)});
        }
    }

    /**
     * Finishes processing and resets the {@code processing} flag
     * @param recordCount
     */
    private void finishProcessing(Integer recordCount) {
        if (processing.compareAndSet(true, false)) {
            if (recordCount > 0) {
                getLogger().info("Reporting Task Finished.  Last Event Recorded was: {} ", new Object[]{getLastEventId(stateManager)});
            }
        }
    }

    /**
     *  * Responsible to querying the provenance data and sending the events to Kylo, both the streaming event aggregration and the batch event data A boolean {@code processing} flag is used to prevent
     * multiple threads from running this trigger at the same time. 1. sets the Boolean flag to processing 2. queries NiFi provenance to determine the set of Events to process and send to Kylo 3.
     * aggregrates and processes the batch events and sends to Kylo via JMS 4. Callback listeners for the JMS will update the {@code StateManager} setting the {@code LAST_EVENT_ID_KEY} value. 5. Upon
     * any failure the {@code abortProcessing()} will be called
     * @param context
     */
    @Override
    public void onTrigger(final ReportingContext context) {

        String nodeId = getNodeIdStrategy().getNodeId(context);
        getLogger().debug("Nifi nodeId {}", new Object[]{nodeId});
        if (nodeId == null) {
            return;
        }

        ensureInitializeFlowFileMapDbCache();

        if (!isInitializing() && processing.compareAndSet(false, true)) {

            final StateManager stateManager = context.getStateManager();
            final EventAccess access = context.getEventAccess();
            final ProvenanceEventRepository provenance = access.getProvenanceRepository();

            if (this.stateManager == null) {
                this.stateManager = stateManager;
            }
            getLogger().debug("KyloProvenanceEventReportingTask onTrigger Info: Reporting Task Triggered!  Last Event id is ", new Object[]{getLastEventId(stateManager)});
            ensureJmsListeners();

            //get the latest event Id in provenance
            final Long maxEventId = provenance.getMaxEventId();
            if (maxEventId == null || maxEventId < 0) {
                getLogger().debug("No Provenance exists yet.  Max Id is not set.. Will not process events ");
                finishProcessing(0);
                return;
            }
            previousMax = maxEventId;
            try {

                if (!isKyloAvailable()) {
                    getLogger().info("Kylo is not available to process requests yet.  This task will exit and wait for its next schedule interval.");
                    abortProcessing();
                    return;
                }

                //get the last event that was processed
                long lastEventId = initializeAndGetLastEventIdForProcessing(maxEventId, nodeId);

                //finish processing if there is nothing to process
                if (lastEventId == maxEventId.longValue()) {
                    getLogger().trace("Last event id == max id... will not process!");
                    finishProcessing(0);
                    return;
                }

                DateTime lastLogTime = DateTime.now();
                //how often to report the batch processing log when processing a lot of events
                int logReportingTimeMs = 10000; //every 10 sec
                long nextId = lastEventId + 1;

                //record count is inclusive, so we need to add one to the difference to include the last eventid
                int recordCount = new Long(maxEventId - (nextId < 0 ? 0 : nextId)).intValue() + 1;
                int totalRecords = recordCount;
                long start = System.currentTimeMillis();
                //split this into batches of events, maxing at 500 if not specified
                int batchSize = processingBatchSize == null || processingBatchSize < 1 ? 500 : processingBatchSize;
                //setup the object pool to be able to store at least the processing batch size amount
                ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
                int total = processingBatchSize + 100;
                pool.setMaxIdle(total);
                pool.setMaxTotal(total);

                Integer batches = (int) Math.ceil(Double.valueOf(recordCount) / batchSize);
                if (recordCount > 0) {
                    getLogger().info(
                        "KyloProvenanceEventReportingTask onTrigger Info: KyloFlowCache Sync Id: {} . Attempting to process {} events starting with event id: {}.  Splitting into {} batches of {} each ",
                        new Object[]{nifiFlowSyncId, recordCount, nextId, batches, batchSize});
                }

                //reset the queryTime holder
                nifiQueryTime = 0L;
                while (recordCount > 0) {
                    if (!isProcessing()) {
                        break;
                    }
                    long min = lastEventId + 1;
                    long max = (min + (batchSize - 1)) > maxEventId ? maxEventId : (min + (batchSize - 1));
                    int batchAmount = new Long(max - (min < 0 ? 0 : min)).intValue() + 1;
                    if (batchAmount <= 0) {
                        break;
                    } else {
                        lastEventId = processEventsInRange(provenance, min, max);
                        recordCount -= batchAmount;
                        recordCount = recordCount < 0 ? 0 : recordCount;
                        setLastEventId(lastEventId);

                        if (lastLogTime == null || (DateTime.now().getMillis() - lastLogTime.getMillis() > logReportingTimeMs)) {
                            lastLogTime = DateTime.now();
                            getLogger().info(
                                "KyloProvenanceEventReportingTask onTrigger Info: ReportingTask is in a long running process.  Currently processing Event id: {}.  {} events remaining to be processed. ",
                                new Object[]{lastEventId, recordCount});
                        }
                    }
                    if (!isProcessing()) {
                        break;
                    }


                }
                if (totalRecords > 0 && isProcessing()) {
                    long processingTime = (System.currentTimeMillis() - start);
                    getLogger().info(
                        "KyloProvenanceEventReportingTask onTrigger Info: ReportingTask finished. Last Event id: {}. Total time to process {} events was {} ms.  Total time spent querying for events in Nifi was {} ms.  Kylo ProcessingTime: {} ms ",
                        new Object[]{lastEventId, totalRecords, processingTime, nifiQueryTime, processingTime - nifiQueryTime});
                }

                finishProcessing(totalRecords);

            } catch (IOException e) {
                getLogger().error(e.getMessage(), e);
            } finally {
                abortProcessing();
            }
        } else {
            if (isInitializing()) {
                getLogger().info("Still initializing any previously active flow file provenance data.  The task should run shortly");
            } else {
                Long maxId = context.getEventAccess().getProvenanceRepository().getMaxEventId();
                Long count = (maxId - previousMax);
                getLogger().info(
                    "KyloProvenanceEventReportingTask onTrigger Info: Still processing previous batch " + currentProcessingMessage + ".  The next run will process events up to " + maxId + ". " + count
                    + " new events");
            }
        }
    }

    /**
     *
     * @return
     */
    private NodeIdStrategy getNodeIdStrategy() {
        if (nodeIdStrategy == null) {
            nodeIdStrategy = SpringApplicationContext.getInstance().getBean(NodeIdStrategy.class);
        }
        return nodeIdStrategy;

    }

    /**
     * Check to see if Kylo is up and running and ready to process
     */
    private boolean isKyloAvailable() {
        boolean avalable = false;
        try {
            avalable = getKyloNiFiFlowProvider().isNiFiFlowDataAvailable();
        } catch (Exception e) {
            getLogger().error("Error checking to see if Kylo is available. Please ensure Kylo is up and running. ");
        }
        return avalable;
    }

    /**
     * Flag to indicate the current reporting task is running
     * @return
     */
    private boolean isProcessing() {
        return processing.get();
    }

    /**
     * flag to indicate the current reporting task is initializing
     * @return
     */
    private boolean isInitializing() {
        return initializing.get();
    }


    /**
     * Set the lastEventId for processing based upon the strategies set in the Reporting Task
     * @param maxEventId the max id found in this provenance repository
     * @param clusterNodeId the name of the cluster node
     * @return the lastEventId to use for processing the and querying the events.  This will be the Min Event Id Used when querying NiFi for the events
     * @throws IOException
     */
    private Long initializeAndGetLastEventIdForProcessing(Long maxEventId, String clusterNodeId) throws IOException {
        long lastEventId = getLastEventId(stateManager);

        //Reset to the beginning if provenance restarts its counts
        if (lastEventId != -1 && lastEventId > maxEventId) {
            getLogger().warn(
                "KyloProvenanceEventReportingTask EventId Warning: Current provenance max id is {} which is less than what was stored in state as the last queried event, which was {}. This means the provenance restarted its "
                +
                "ids. Restarting querying from the beginning.", new Object[]{maxEventId, lastEventId});
            lastEventId = -1;
            lastEventIdInitialized = true;
            setLastEventId(lastEventId);
            return lastEventId;
        }

        //check to see if we should reset the range query
        if (initialId == null) {
            initialId = lastEventId;
            if (initialEventIdValue.equals(INITIAL_EVENT_ID_OPTION.MAX_EVENT_ID)) {
                // initialize the initial id
                getLogger().info(
                    "KyloProvenanceEventReportingTask EventId Info: Since the INITIAL_EVENT_ID parameter is set to 'MAX_EVENT_ID' setting the initial event id to be equal to the maxEventId of {}.  No events will be procssed until the next trigger. ",
                    new Object[]{maxEventId});
                setLastEventId(maxEventId);
                lastEventId = maxEventId - 1;
                initialId = maxEventId - 1;
            } else if (initialEventIdValue.equals(INITIAL_EVENT_ID_OPTION.KYLO)) {

                try {
                    getLogger().info("KyloProvenanceEventReportingTask EventId Info: Attempting to set the initial event id to be equal to the maxEventId from Kylo. Nifi Cluster NodeId: {} ",
                                     new Object[]{(StringUtils.isNotBlank(clusterNodeId) ? clusterNodeId : "N/A")});
                    lastEventId = getKyloNiFiFlowProvider().findNiFiMaxEventId(clusterNodeId);
                    if (lastEventId == 0) {
                        lastEventId = -1;
                    }
                    setLastEventId(lastEventId);
                    initialId = lastEventId;
                    getLogger().info("KyloProvenanceEventReportingTask EventId Info: Successfully obtained and set the initial event id to be equal to the maxEventId from Kylo as {}  ",
                                     new Object[]{lastEventId});

                } catch (Exception e) {
                    getLogger().error("KyloProvenanceEventReportingTask EventId Error: Unable to set initial event id from Kylo. The last event id is {} ", new Object[]{lastEventId}, e);
                }

            }
        }

        //check to see if we should default the not found event id
        if ((!lastEventIdInitialized && lastEventId == -1)) {
            if (lastEventIdNotFoundValue.equals(LAST_EVENT_ID_NOT_FOUND_OPTION.MAX_EVENT_ID)) {
                // initialize the initial id
                getLogger().info(
                    "KyloProvenanceEventReportingTask EventId Info: Unable to find the last event id.  This is possible because this is the first time the Reporting Task was setup.  Since the  LAST_EVENT_ID parameter is set to 'MAX_EVENT_ID' setting the last event id to be equal to the maxEventId of {}.  No events will be processed until the next trigger. ",
                    new Object[]{maxEventId});
                setLastEventId(maxEventId);
                lastEventId = maxEventId - 1;
                lastEventIdInitialized = true;
            } else if (lastEventIdNotFoundValue.equals(LAST_EVENT_ID_NOT_FOUND_OPTION.KYLO)) {
                try {
                    getLogger().info("KyloProvenanceEventReportingTask EventId Info: Attempting to set the last event event id to be equal to the maxEventId from Kylo.  Nifi Cluster NodeId: {}",
                                     new Object[]{(StringUtils.isNotBlank(clusterNodeId) ? clusterNodeId : "N/A")});
                    lastEventId = getKyloNiFiFlowProvider().findNiFiMaxEventId(clusterNodeId);
                    if (lastEventId == 0) {
                        lastEventId = -1;
                    }
                    setLastEventId(lastEventId);
                    getLogger().info("KyloProvenanceEventReportingTask EventId Info: Set the last event id to be equal to the maxEventId from Kylo as {}  ", new Object[]{lastEventId});
                } catch (Exception e) {
                    getLogger().error(
                        "KyloProvenanceEventReportingTask EventId Error: Unable to set last event id from Kylo. It will be set to the Provenance maxEventId {}.  No events will be processed until the next trigger ",
                        new Object[]{maxEventId}, e);
                    setLastEventId(maxEventId);
                    lastEventId = maxEventId - 1;
                }
                lastEventIdInitialized = true;

            }
        }

        return lastEventId;
    }

    /**
     * processes all events inclusive in the range
     * @param provenance the repository to query
     * @param minEventId the minEventId to query
     * @param maxEventId the maxEvent id to query
     * @return the lastEventId processed
     * @throws IOException
     */
    private Long processEventsInRange(ProvenanceEventRepository provenance, Long minEventId, Long maxEventId) throws IOException {
        Long lastEventId = null;

        int recordCount = new Long(maxEventId - (minEventId < 0 ? 0 : minEventId)).intValue();
        currentProcessingMessage = "Finding all Events between " + minEventId + " - " + maxEventId;

        //add one to the record count to get the correct number in the range including the maxEventId
        recordCount += 1;
        long start = System.currentTimeMillis();
        final List<ProvenanceEventRecord> events = provenance.getEvents(minEventId, recordCount);

        updateNifiFlowCache();

        long end = System.currentTimeMillis();
        nifiQueryTime += (end - start);
        Collections.sort(events, new ProvenanceEventRecordComparator());
        ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
        List<ProvenanceEventRecordDTO> pooledEvents = new ArrayList<>(events.size());
        try {
            for (ProvenanceEventRecord eventRecord : events) {
                if (!isProcessing()) {
                    break;
                }
                if (lastEventId == null || eventRecord.getEventId() != lastEventId) {
                    ProvenanceEventRecordDTO dto = processEvent(eventRecord);
                    if (dto != null) {
                        pooledEvents.add(dto);
                    }
                }
                lastEventId = eventRecord.getEventId();
            }
            //Send JMS off
            getProvenanceEventCollector().sendToJms();
        } catch (Exception e) {
            getLogger().error("Error processing Kylo ProvenanceEvent ", e);
            abortProcessing();
        } finally {
            //return the objects back to the pool
            pooledEvents.stream().forEach(dto -> {
                if (dto != null) {
                    try {
                        dto.reset();
                        pool.returnObject(dto);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            pooledEvents.clear();

        }
        getLogger().info("ProvenanceEventPool: Pool Stats: Created:[" + pool.getCreatedCount() + "], Borrowed:[" + pool.getBorrowedCount() + "]");

        return lastEventId == null ? minEventId : lastEventId;

    }

    /**
     *  Process the Event, calculate the  statistics and send it on to JMS for Kylo Ops manager processing
     * If the event is not found to be managed by Kylo it is returned as Null.
     * @param event the event to process
     * @return the converted event after being processed.
     * @throws Exception
     */
    public ProvenanceEventRecordDTO processEvent(ProvenanceEventRecord event) throws Exception {
        ProvenanceEventRecordDTO eventRecordDTO = null;
        if (getProvenanceFeedLookup().isKyloManaged(event.getComponentId())) {
            ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
            eventRecordDTO = ProvenanceEventRecordConverter.getPooledObject(pool, event);
            getProvenanceEventCollector().process(eventRecordDTO);
        }
        return eventRecordDTO;
    }

    /**
     * Ensures that the JMS Listeners are in place, if not creates them
     */
    private void ensureJmsListeners() {
        if (batchJmsListener == null) {
            batchJmsListener = new KyloReportingTaskJmsListeners.KyloReportingTaskBatchJmsListener(this);
            getProvenanceEventActiveMqWriter().subscribe(batchJmsListener);
        }
        if (statsJmsListener == null) {
            this.statsJmsListener = new KyloReportingTaskJmsListeners.KyloReportingTaskStatsJmsListener(this);
            getProvenanceEventActiveMqWriter().subscribe(statsJmsListener);
        }
    }

    /***
     * Gets the Spring managed bean to retrieve Kylo feed information
     * @return
     */
    private ProvenanceFeedLookup getProvenanceFeedLookup() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceFeedLookup.class);
    }

    /**
     * The Spring managed bean to collect and process the event for Kylo
     * @return
     */
    private ProvenanceEventCollector getProvenanceEventCollector() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventCollector.class);
    }

    /**
     * The Spring managed bean to send the events to JMS for Kylo
     * @return
     */
    private ProvenanceEventActiveMqWriter getProvenanceEventActiveMqWriter() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventActiveMqWriter.class);
    }

    /**
     * The ProvenanceEventRecordDTO object pool
     * @return
     */
    private ProvenanceEventObjectPool getProvenanceEventObjectPool() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventObjectPool.class);
    }

    /**
     * Persistent cache that will only be used when NiFi shuts down or is started, persisting the RootFlowFile objects to help complete Statistics and event processing when NiFi shuts down with events
     * in mid flow processing
     */
    private FeedFlowFileMapDbCache getFlowFileMapDbCache() {
        return SpringApplicationContext.getInstance().getBean(FeedFlowFileMapDbCache.class);
    }


    /**
     * Comparator sorting events by eventId
     */
    public class ProvenanceEventRecordComparator implements Comparator<ProvenanceEventRecord> {


        public int compare(ProvenanceEventRecord o1, ProvenanceEventRecord o2) {
            if (o1 == null && o1 == null) {
                return 0;
            } else if (o1 != null && o2 == null) {
                return -1;
            } else if (o1 == null && o2 != null) {
                return 1;
            } else {
                return new Long(o1.getEventId()).compareTo(new Long(o2.getEventId()));
            }
        }
    }


}
