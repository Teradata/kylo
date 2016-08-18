package com.thinkbiganalytics.nifi.provenance.v2.cache.flow;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.flow.controller.NifiFlowClient;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16. Cache of the Nifi Flow graph
 * //TODO Might only be needed to help determine if a FlowFile is complete comparing it to the process graph.
 * //Currently set to not active until further discovery on event data
 */
public class NifiFlowCache {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCache.class);

    private boolean active = true;

    private NifiFlowClient nifiFlowClient;

    private Integer MAX_SIZE = 100;

    private static NifiFlowCache instance = new NifiFlowCache();

    public static NifiFlowCache instance() {
        return instance;
    }

    private void initClient() {
        if (active) {
            nifiFlowClient = new NifiFlowClient(URI.create("http://localhost:8079"));
        }
    }

    private final LoadingCache<String, NifiFlowProcessGroup> feedFlowCache;

    //mapping to get stats outside of a feed for each processor (regardless of feed).
    //map processorid to list of SimpleNifiFlowProcessor objects

    private ConcurrentHashMap<String, NifiFlowProcessor> startingFeedProcessors = new ConcurrentHashMap<>();


    private NifiFlowCache() {
        log.info("Create NifiFlowCache");
        initClient();
        log.info("Starting to NifiFlowCache setup cache {}", nifiFlowClient);

        feedFlowCache = CacheBuilder.newBuilder().recordStats().maximumSize(MAX_SIZE).build(new CacheLoader<String, NifiFlowProcessGroup>() {
                                                                                                @Override
                                                                                                public NifiFlowProcessGroup load(String processGroupId) throws Exception {
                                                                                                    NifiFlowProcessGroup group = getGraph(processGroupId);
                                                                                                    return group;
                                                                                                }
                                                                                            }
        );

        log.info("Cache setup... load All into cache ");

        loadAll();




    }

    private NifiFlowProcessGroup getGraph(String processGroupId) {
        if (nifiFlowClient != null) {

            log.info(" START load for ProcessGroup {} ", processGroupId);
            NifiFlowProcessGroup group = nifiFlowClient.getFlowForProcessGroup(processGroupId);
            assignStartingProcessors(group);
            log.info(" Finish load for ProcessGroup {} , {} ", processGroupId, group);
            return group;
        }
        return null;
    }

    private void assignStartingProcessors(NifiFlowProcessGroup group) {
        if (group != null) {
            group.getStartingProcessors().stream().forEach(processor -> startingFeedProcessors.put(processor.getId(), processor));
        }
    }


    public void loadAll() {
        log.info(" START loadALL ");
        if (nifiFlowClient != null) {
            Map<String, NifiFlowProcessGroup> map = new HashMap<>();
            List<NifiFlowProcessGroup> allFlows = nifiFlowClient.getAllFlows();
            log.info("Finished Loading ALL");
            if (allFlows != null) {
                map = allFlows.stream().collect(
                    Collectors.toMap(simpleNifiFlowProcessGroup -> simpleNifiFlowProcessGroup.getId(), simpleNifiFlowProcessGroup -> simpleNifiFlowProcessGroup));
                map.values().forEach(group -> assignStartingProcessors(group));
                feedFlowCache.putAll(map);
            }
        }

    }

    /**
     * Gets a Processor by the parent Feed ProcessGroup and the id of the processor
     */
    public NifiFlowProcessor getStartingProcessor(String processorId) {
        return startingFeedProcessors.get(processorId);
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
        NifiFlowProcessor startingProcessor = getStartingProcessor(flowFile.getRootFlowFile().getFirstEvent().getComponentId());
        if (startingProcessor != null) {
            return startingProcessor.getProcessGroup();
        }
        return null;
    }

    public void setNifiFlowClient(NifiFlowClient nifiFlowClient) {
        this.nifiFlowClient = nifiFlowClient;
    }
}
