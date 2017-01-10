package com.thinkbiganalytics.nifi.provenance.reporting;

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventAggregator;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.cache.FlowFileMapDbCache;
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
 * Created by sr186054 on 11/7/16.
 */
@Tags({"reporting", "kylo", "provenance"})
@CapabilityDescription("Publishes Provenance Events to the JMS queues for Kylo")
public class KyloProvenanceEventReportingTask extends AbstractReportingTask {

    public static final String LAST_EVENT_ID_KEY = "kyloLastEventId";


    PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("Think Big metadata service")
        .required(true)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    public static final PropertyDescriptor SPRING_SERVICE = new PropertyDescriptor.Builder()
        .name("Spring Context Service")
        .description("Service for loading spring a spring context and providing bean lookup")
        .required(true)
        .identifiesControllerService(SpringContextService.class)
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
        .description("The size of grouped events sent over to Kylo")
        .defaultValue("50")
        .required(false)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor REBUILD_CACHE_ON_RESTART = new PropertyDescriptor.Builder()
        .name("Rebuild Cache on restart")
        .description(
            "Should the cache of the flows be rebuilt every time the Reporting task is restarted?  By default the system will keep the cache up to date; however, setting this to true will force the cache to be rebuilt upon restarting the reporting task. ")
        .required(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue("false")
        .expressionLanguageSupported(true)
        .build();

    private SpringContextService springService;
    private MetadataProviderService metadataProviderService;

    /**
     * Flag to indicate if the task is running and processing to prevent multiple threads from executing it
     */
    private AtomicBoolean processing = new AtomicBoolean(false);

    /**
     * Flag to indicate the system has started and it is loading any data from the persisted cache
     */
    private AtomicBoolean initializing = new AtomicBoolean(false);

    /**
     * The Id used to sync with the Kylo Metadata to build the cache of NiFi processorIds in helping to determine the Flows Feed and Flows Failure Processors
     */
    private String nifiFlowSyncId = null;

    /**
     * Pointer to the NiFi StateManager used to store the lastEventId processed
     */
    private StateManager stateManager;


    private KyloReportingTaskJmsListeners.KyloReportingTaskBatchJmsListener batchJmsListener;

    private KyloReportingTaskJmsListeners.KyloReportingTaskStatsJmsListener statsJmsListener;

    /**
     * Store the value of the ReportingTask that indiciates the failsafe in case a flow starts processing a lot of events very quick
     */
    private Integer maxBatchFeedJobEventsPerSecond;

    /**
     * Events are sent over JMS and partitioned into smaller batches.
     */
    private Integer jmsEventGroupSize;


    private String currentProcessingMessage = "";

    private Long previousMax = 0L;

    private boolean rebuildOnRestart = false;


    public KyloProvenanceEventReportingTask() {
        super();
    }


    @Override
    public void init(final ReportingInitializationContext context) {
        getLogger().info("init of KyloReportingTask");

    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(METADATA_SERVICE);
        properties.add(SPRING_SERVICE);
        properties.add(MAX_BATCH_FEED_EVENTS_PER_SECOND);
        properties.add(JMS_EVENT_GROUP_SIZE);
        properties.add(REBUILD_CACHE_ON_RESTART);
        return properties;
    }


    @OnScheduled
    public void setup(final ConfigurationContext context) throws IOException, InitializationException {
        getLogger().info("OnScheduled of KyloReportingTask");
        loadSpring();
        this.metadataProviderService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        this.springService = context.getProperty(SPRING_SERVICE).asControllerService(SpringContextService.class);
        this.maxBatchFeedJobEventsPerSecond = context.getProperty(MAX_BATCH_FEED_EVENTS_PER_SECOND).asInteger();
        this.jmsEventGroupSize = context.getProperty(JMS_EVENT_GROUP_SIZE).asInteger();
        getProvenanceEventAggregator().setMaxBatchFeedJobEventsPerSecond(this.maxBatchFeedJobEventsPerSecond);
        getProvenanceEventAggregator().setJmsEventGroupSize(this.jmsEventGroupSize);
        Boolean rebuildOnRestart = context.getProperty(REBUILD_CACHE_ON_RESTART).asBoolean();

        if (rebuildOnRestart != null) {
            this.rebuildOnRestart = rebuildOnRestart;
        }
        if (this.rebuildOnRestart && StringUtils.isNotBlank(nifiFlowSyncId)) {
            nifiFlowSyncId = null;
        }
    }

    @OnStopped
    public void onStopped(ConfigurationContext configurationContext) {
        abortProcessing();
    }


    /**
     * When shutting down the ActiveFlowFile information is persisted to Disk
     */
    @OnShutdown
    public final void onShutdown(ConfigurationContext configurationContext) {
        getLogger().info("onShutdown: Attempting to persist any active flow files to disk");
        abortProcessing();
        getLogger().info("onShutdown: Ensure Spring beans are wired");
        loadSpring();
        //persist running flowfile metadata to disk
        int persistedRootFlowFiles = getFlowFileMapDbCache().persistActiveRootFlowFiles();
        getLogger().info("onShutdown: Finished persisting {} root flow files to disk ", new Object[]{persistedRootFlowFiles});
    }


    /**
     * When NiFi comes up load any ActiveFlowFiles that have been persisted to disk
     */
    @OnConfigurationRestored
    public final void onConfigurationRestored() {
        if (initializing.compareAndSet(false, true)) {
            try {
                getLogger().info("onConfigurationRestored: Attempting to load any persisted files from disk into the Guava Cache");

                loadSpring();
                //rebuild mem flowfile metadata from disk
                int loadedRootFlowFiles = getFlowFileMapDbCache().loadGuavaCache();
                getLogger().info("onConfigurationRestored: Finished loading {} persisted files from disk into the Guava Cache", new Object[]{loadedRootFlowFiles});
            } catch (Exception e) {
                getLogger().error("ERROR on onConfigurationRestored {} ", new Object[]{e.getMessage()}, e);
            } finally {
                initializing.set(false);
            }
        }
    }


    private void loadSpring() {
        getLogger().info("loadSpring spring configurations");
        try {
            SpringApplicationContext.getInstance().initializeSpring();
            getLogger().info("LOADED spring configurations");
        } catch (BeansException | IllegalStateException e) {
            getLogger().info("Failed to load spring configurations", e);
        }
    }


    private KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
        return metadataProviderService.getKyloNiFiFlowProvider();
    }

    /**
     * Gets the last Event that was saved in the StateManager the LastEventId is saved via a JMS Listener when the event is sent to JMS a callback is executed and then it updates the StateManager via
     * this method
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
        getLogger().info("KyloReportingTask EventId Info: Setting the Last Event Id to be {} ", new Object[]{eventId});
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
            getLogger().info("Finished updateNifiFlowCache with syncId: {}, mapSize: {} ", new Object[]{nifiFlowSyncId, getProvenanceFeedLookup().getProcessorIdMapSize()});
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
     */
    private void finishProcessing(Integer recordCount) {
        if (processing.compareAndSet(true, false)) {
            if (recordCount > 0) {
                getLogger().info("Reporting Task Finished.  Last Event Recorded was: {} ", new Object[]{getLastEventId(stateManager)});
            }
        }
    }


    /**
     * Responsible to querying the provenance data and sending the events to Kylo, both the streaming event aggregration and the batch event data A boolean {@code processing} flag is used to prevent
     * multiple threads from running this trigger at the same time. 1. sets the Boolean flag to processing 2. queries NiFi provenance to determine the set of Events to process and send to Kylo 3.
     * aggregrates and processes the batch events and sends to Kylo via JMS 4. Callback listeners for the JMS will update the {@code StateManager} setting the {@code LAST_EVENT_ID_KEY} value. 5. Upon
     * any failure the {@code abortProcessing()} will be called
     */
    @Override
    public void onTrigger(final ReportingContext context) {

        if (processing.compareAndSet(false, true) && initializing.get() == false) {

            getLogger().info("Reporting Task Triggered!");

            if (metadataProviderService != null) {
                getLogger().info("Found Metadata Service Provider as {}", new Object[]{metadataProviderService});
            } else {
                getLogger().info("Unable to find Metadata Service Provider ");
            }

            final StateManager stateManager = context.getStateManager();
            final EventAccess access = context.getEventAccess();
            final ProvenanceEventRepository provenance = access.getProvenanceRepository();

            if (this.stateManager == null) {
                this.stateManager = stateManager;
            }
            ensureJmsListeners();

            //get the latest event Id in provenance
            final Long maxEventId = provenance.getMaxEventId();
            previousMax = maxEventId;
            try {
                //get the last event that was processed
                long lastEventId = getLastEventId(stateManager);
                if (lastEventId > maxEventId) {
                    getLogger().info("KyloReportingTask EventId Info: The last saved eventId of {} is > then the reported maxEventId of {} in NiFi Provenance. Resetting the lastEventId to be 0.",
                                     new Object[]{lastEventId, maxEventId});
                    lastEventId = -1;
                }

                //saved 2
                //next 3
                // max 4
                // 3, 4-2 2  3,4
                long nextId = lastEventId + 1;
                int recordCount = new Long(maxEventId - (lastEventId < 0 ? 0 : lastEventId)).intValue();
                if (recordCount > 0) {
                    getLogger().info("KyloReportingTask EventId Info: Finding {} events between {} - {} ", new Object[]{recordCount, nextId, maxEventId});
                }
                currentProcessingMessage = "Finding all Events between " + nextId + " - " + maxEventId;

                final List<ProvenanceEventRecord> events = provenance.getEvents(nextId, recordCount);
                //update NiFiFlowCache with list of changes for processing the events
                updateNifiFlowCache();
                Collections.sort(events, new ProvenanceEventRecordComparator());
                for (ProvenanceEventRecord eventRecord : events) {
                    if (!processing.get()) {
                        break;
                    }
                    if (eventRecord.getEventId() != lastEventId) {
                        processEvent(eventRecord);
                    }
                    lastEventId = eventRecord.getEventId();
                }
                //Send JMS off
                getProvenanceEventAggregator().sendToJms();
                if (recordCount > 0) {
                    getLogger().info("KyloReportingTask EventId Info: Finished  Event id: " + Long.toString(lastEventId));
                }
                finishProcessing(recordCount);
            } catch (IOException e) {
                getLogger().error(e.getMessage(), e);
                abortProcessing();
                return;
            }
        } else {
            if (initializing.get()) {
                getLogger().info("Still initializing any previously active flow file provenance data.  The task should run shortly");
            } else {
                Long maxId = context.getEventAccess().getProvenanceRepository().getMaxEventId();
                Long count = (maxId - previousMax);
                getLogger().info("Still processing previous batch " + currentProcessingMessage + ".  The next run will process events up to " + maxId + ". " + count + " new events");
            }
        }
    }

    /**
     * Process the Event, calculate the Aggregrate statistics and send it on to JMS for Kylo Ops manager processing
     *
     * @param event the event to process
     */
    public ProvenanceEventRecordDTO processEvent(ProvenanceEventRecord event) {
        ProvenanceEventRecordDTO eventRecordDTO = ProvenanceEventRecordConverter.convert(event);
        //getLogger().info("EVENT for {} - {} is {}.",new Object[] {event.getComponentType(),event.getEventId()});
        getProvenanceEventAggregator().process(eventRecordDTO);
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


    private ProvenanceFeedLookup getProvenanceFeedLookup() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceFeedLookup.class);
    }

    private ProvenanceEventAggregator getProvenanceEventAggregator() {
        return (ProvenanceEventAggregator) SpringApplicationContext.getInstance().getBean(ProvenanceEventAggregator.class);
    }

    private ProvenanceEventActiveMqWriter getProvenanceEventActiveMqWriter() {
        return (ProvenanceEventActiveMqWriter) SpringApplicationContext.getInstance().getBean(ProvenanceEventActiveMqWriter.class);
    }


    private FlowFileMapDbCache getFlowFileMapDbCache() {
        return SpringApplicationContext.getInstance().getBean(FlowFileMapDbCache.class);
    }


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
