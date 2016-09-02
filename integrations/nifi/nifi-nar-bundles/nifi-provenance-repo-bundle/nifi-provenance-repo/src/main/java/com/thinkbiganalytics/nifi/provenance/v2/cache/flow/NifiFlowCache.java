package com.thinkbiganalytics.nifi.provenance.v2.cache.flow;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.flow.controller.NifiFlowClient;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 8/11/16. Cache of the Nifi Flow graph
 * TODO Block and loadAll upon startup
 */
@Component
public class NifiFlowCache {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCache.class);

    private boolean active = true;


    private NifiFlowClient nifiFlowClient;

   // private static NifiFlowCache instance = new NifiFlowCache();
    private DateTime loadAllTime;
    private AtomicBoolean loading = new AtomicBoolean(false);


    private Integer maxConnectionRetryAttempts = 30;
    private AtomicInteger connectionRetryAttempts = new AtomicInteger(0);
    private AtomicInteger maxConnectionAttemptsReachedWaitCounter = new AtomicInteger(0);


    private AtomicBoolean isConnectionCheckTimerRunning = new AtomicBoolean(false);

   // public static NifiFlowCache instance() {
   //     return instance;
  //  }

    @Value("${thinkbig.nifi.rest.host}")
    private String host;

    @Value("${thinkbig.nifi.rest.username}")
    private String username;

    @Value("${thinkbig.nifi.rest.password}")
    private String password;

    private void initClient() {
        if (active) {
            if(StringUtils.isNotBlank(username)){
                log.info("attempt to create new NifiFlowClient using {}:<PASSWORD>@ {}",username,host);
                nifiFlowClient = new NifiFlowClient(URI.create(host), NifiFlowClient.createCredentialProvider(username,password));
            }
            else {
                log.info("attempt to create new NifiFlowClient using anonymous user @ {}",host);
                nifiFlowClient = new NifiFlowClient(URI.create("http://localhost:8079"));
            }

            //check the connection every second
            initConnectionCheckTimerThread(0,1*1000,10);
        }
    }
    private boolean isConnectedToNifiRest(){
        if(nifiFlowClient != null){
            boolean isConnected = nifiFlowClient.isConnected();
            return isConnected;
        }
        return false;
    }

    private final LoadingCache<String, NifiFlowProcessGroup> feedFlowCache;

    //mapping to get stats outside of a feed for each processor (regardless of feed).
    //map processorid to list of SimpleNifiFlowProcessor objects

    private ConcurrentHashMap<String, NifiFlowProcessor> startingFeedProcessors = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> processorNameMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<String, NifiFlowProcessor> processorIdProcessorMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<NifiFlowProcessor>> destinationConnectionIdProcessorMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<String, List<NifiFlowProcessor>> failureConnectionIdToSourceProcessorMap = new ConcurrentHashMap<>();



    private NifiFlowCache() {
        log.info("Create NifiFlowCache");
        feedFlowCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, NifiFlowProcessGroup>() {
                                                                          @Override
                                                                          public NifiFlowProcessGroup load(String processGroupId) throws Exception {
                                                                              NifiFlowProcessGroup group = getGraph(processGroupId);
                                                                              return group;
                                                                          }
                                                                      }
        );

    }

    @PostConstruct
    private void initAndLoadCache(){
        log.info("Post Construct of NifFlowClient!!");
        initClient();
    }

    private NifiFlowProcessGroup getGraph(String processGroupId) {
        if (nifiFlowClient != null) {
            log.info(" START load for ProcessGroup {} ", processGroupId);
            NifiFlowProcessGroup group = nifiFlowClient.getFlowForProcessGroup(processGroupId);
            populateStartingProcessors(group);
            populateProcessorMaps(group);
            log.info(" Finish load for ProcessGroup {} , {} ", processGroupId, group);
            return group;
        }
        return null;
    }

    private void populateStartingProcessors(NifiFlowProcessGroup group) {
        if (group != null) {
            group.getStartingProcessors().stream().forEach(processor -> startingFeedProcessors.put(processor.getId(), processor));
        }
    }


    private void populateProcessorMaps(NifiFlowProcessGroup group) {
        group.getProcessorMap().values().stream().forEach(nifiFlowProcessor -> {
            processorIdProcessorMap.put(nifiFlowProcessor.getId(), nifiFlowProcessor);
            processorNameMap.put(nifiFlowProcessor.getId(), nifiFlowProcessor.getName());

            nifiFlowProcessor.getDestinationConnectionIds().forEach(niFiFlowProcessorConnection -> {
                destinationConnectionIdProcessorMap.computeIfAbsent(niFiFlowProcessorConnection.getConnectionIdentifier(), (id) -> new ArrayList<>()).add(nifiFlowProcessor);
            });
        });
        if(group.getFailureConnectionIdToSourceProcessorMap() != null){
            failureConnectionIdToSourceProcessorMap.putAll(group.getFailureConnectionIdToSourceProcessorMap());
        }
    }

    public NifiFlowProcessor getProcessor(String processorId) {
        return processorIdProcessorMap.get(processorId);
    }


    private void loadAll() {
        log.info(" Start to load all flows into cache ");
        long start = System.currentTimeMillis();
        if (nifiFlowClient != null && loading.compareAndSet(false, true)) {
            List<NifiFlowProcessGroup> allFlows = nifiFlowClient.getAllFlows();
            if (allFlows != null) {
                Map<String, NifiFlowProcessGroup> map = allFlows.stream().collect(
                    Collectors.toMap(simpleNifiFlowProcessGroup -> simpleNifiFlowProcessGroup.getId(), simpleNifiFlowProcessGroup -> simpleNifiFlowProcessGroup));
                map.values().forEach(group -> {
                    populateStartingProcessors(group);
                    populateProcessorMaps(group);
                });

                feedFlowCache.putAll(map);
            }
            loadAllTime = DateTime.now();
            log.info("Finished Loading Feed flow cache.  size: {}.  Time took to load:  {} ms ", feedFlowCache.asMap().size(), (System.currentTimeMillis() - start));
            loading.set(false);
        }

    }

    /**
     * Gets a Processor by the parent Feed ProcessGroup and the id of the processor
     */
    public NifiFlowProcessor getStartingProcessor(String processorId) {
        return startingFeedProcessors.get(processorId);
    }

    public String getProcessorName(String processorId) {
        return processorNameMap.get(processorId);
    }

    public Integer processorNameMapSize() {
        return processorNameMap.size();
    }

    /**
     * Gets a Processor by the parent Feed ProcessGroup and the id of the processor
     */
    public NifiFlowProcessor getProcessor(String feedProcessGroupId, String processorId) {
        try {
            return feedFlowCache.get(feedProcessGroupId).getProcessor(processorId);
        } catch (ExecutionException e) {
            e.printStackTrace();
            //TODO LOG AND THROW RUNTIME
        }
        return null;
    }


    /**
     * Find The Feed Flow for a feed name This will look at the Process Group Name in Nifi and try to match it to the feed name NOTE: this assumes the ProcessGroup name == Feed System Name
     */
    public NifiFlowProcessGroup getFeedFlowForFeedName(String category, String feedName) {
        return feedFlowCache.asMap().values().stream().filter(flow -> (feedName.equalsIgnoreCase(flow.getName()) && category.equalsIgnoreCase(flow.getParentGroupName()))).findAny().orElse(null);
    }

    public NifiFlowProcessGroup getFlow(ActiveFlowFile flowFile) {
        NifiFlowProcessGroup flow = null;
        if (flowFile != null) {
            String firstProcessorId = (flowFile.getRootFlowFile() != null && flowFile.getRootFlowFile().hasFirstEvent()) ? flowFile.getRootFlowFile().getFirstEvent().getComponentId() : null;
            if (firstProcessorId != null) {
                NifiFlowProcessor startingProcessor = getStartingProcessor(firstProcessorId);
                if (startingProcessor != null) {
                    flow = startingProcessor.getProcessGroup();
                } else {
                    //Lock on firstProcessorID
                    synchronized (firstProcessorId) {
                         startingProcessor = getStartingProcessor(firstProcessorId);
                        if (startingProcessor != null) {
                            flow = startingProcessor.getProcessGroup();
                        } else {
                            ///find the processGroup for this first component and then  get the graph
                            ProcessorDTO processorDTO = nifiFlowClient.findProcessorById(firstProcessorId);
                            if (processorDTO != null) {
                                flow = getGraph(processorDTO.getParentGroupId());
                            }
                        }
                    }
                }
            }
        }
        return flow;
    }

    public void setNifiFlowClient(NifiFlowClient nifiFlowClient) {
        this.nifiFlowClient = nifiFlowClient;
    }


    private void initConnectionCheckTimerThread(int start, int interval, int waitCount) {



        if(isConnectionCheckTimerRunning.compareAndSet(false,true)) {
            Timer connectionCheckTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {

                    int retryAttempts =  connectionRetryAttempts.incrementAndGet();
                    if(retryAttempts <= maxConnectionRetryAttempts) {

                        if (isConnectedToNifiRest()) {
                            log.info("Successfully connected to NiFi Rest Client.");
                            if(loadAllTime == null) {
                                loadAll();
                            }
                            connectionCheckTimer.cancel();
                            isConnectionCheckTimerRunning.set(false);
                            connectionRetryAttempts.set(0);
                        }
                        else {
                            log.info("Unable to connect to Nifi Rest.  Attempt Number: {}, Timer will try again in {} seconds ",retryAttempts, interval/1000);
                        }
                    }
                    else {
                            //wait x times  before attempting to check connection
                         int waitCounter = maxConnectionAttemptsReachedWaitCounter.incrementAndGet();
                         if(waitCounter > waitCount) {
                             //reset so the check can happen
                             connectionRetryAttempts.set(0);
                        }
                        else {
                             log.info("Unable to connect to Nifi Rest.  Attempt Number: {}, Timer now wait and try again in {} seconds ",retryAttempts, ((waitCount - waitCounter) * interval) /1000);
                         }

                    }

                }
            };
            connectionCheckTimer.schedule(task, start, interval);
        }

    }


    public String getFailureProcessorWithDestinationConnectionIdentifier(String connectionId) {
         List<NifiFlowProcessor> processors = failureConnectionIdToSourceProcessorMap.get(connectionId);
        if(processors != null){
            return processors.get(0).getId();
        }
        return null;
    }

}
