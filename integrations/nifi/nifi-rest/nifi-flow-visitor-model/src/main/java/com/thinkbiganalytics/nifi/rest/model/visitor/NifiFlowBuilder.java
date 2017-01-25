package com.thinkbiganalytics.nifi.rest.model.visitor;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Created by sr186054 on 8/11/16.
 */
public class NifiFlowBuilder {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowBuilder.class);

    Map<String, NifiFlowProcessor> cache = new ConcurrentHashMap<>();

    /**
     * When walking the flow should it look at the relationship names and identify those processors with a "failure" relationship and mark the processors as potential failure processors
     **/
    private boolean inspectForFailureRelationships = true;

    public NifiFlowBuilder inspectForFailureRelationships(boolean inspectForFailureRelationships) {
        this.inspectForFailureRelationships = inspectForFailureRelationships;
        return this;
    }


    public NifiFlowProcessGroup build(NifiVisitableProcessGroup group) {
        NifiFlowProcessGroup flowProcessGroup = toFlowProcessGroup(group);
        flowProcessGroup.setProcessorMap(cache);
        flowProcessGroup.addConnections(group.getConnections());
        //reassign the failure map
        if (group.getFailureConnectionIdToSourceProcessorIds() != null) {
            group.getFailureConnectionIdToSourceProcessorIds().entrySet().forEach(connectionIdProcessorIdEntry -> {
                if (cache != null && !cache.isEmpty()) {

                    flowProcessGroup.getFailureConnectionIdToSourceProcessorMap().computeIfAbsent(connectionIdProcessorIdEntry.getKey(), (connectionId) -> new ArrayList<>()).addAll(
                        connectionIdProcessorIdEntry.getValue().stream().map(cache::get).collect(Collectors.toList()));
                }
            });
        }

        ProcessGroupDTO groupDTO = group.getParentProcessGroup();
        if (groupDTO != null) {
            flowProcessGroup.setParentGroupId(groupDTO.getId());
            flowProcessGroup.setParentGroupName(groupDTO.getName());
        }

        flowProcessGroup.assignFlowIds();

        return flowProcessGroup;
    }


    private static final Function<ProcessorDTO, NifiFlowProcessor> PROCESSOR_DTO_TO_FLOW_PROCESSOR = new Function<ProcessorDTO, NifiFlowProcessor>() {
        @Override
        public NifiFlowProcessor apply(ProcessorDTO processor) {
            return new NifiFlowProcessor(processor.getId(), processor.getName(), processor.getType());
        }
    };

    private static final Function<ProcessGroupDTO, NifiFlowProcessGroup> PROCESS_GROUP_DTO_TO_FLOW_GROUP = new Function<ProcessGroupDTO, NifiFlowProcessGroup>() {
        @Override
        public NifiFlowProcessGroup apply(ProcessGroupDTO group) {
            return new NifiFlowProcessGroup(group.getId(), group.getName());
        }
    };

    /**
     * Convert a NifiVisitableProcessor to a Simple one
     */
    private final Function<NifiVisitableProcessor, NifiFlowProcessor> NIFI_PROCESSOR_DTO_TO_FLOW_PROCESSOR = new Function<NifiVisitableProcessor, NifiFlowProcessor>() {
        @Nullable
        @Override
        public NifiFlowProcessor apply(NifiVisitableProcessor processor) {
            NifiFlowProcessor flowProcessor = null;
            if (cache.containsKey(processor.getId())) {
                return cache.get(processor.getId());
            }
            flowProcessor = PROCESSOR_DTO_TO_FLOW_PROCESSOR.apply(processor.getDto());
            cache.put(processor.getId(), flowProcessor);
            Set<NifiFlowProcessor> destinations = new HashSet<>(Collections2.transform(processor.getDestinations(), NIFI_PROCESSOR_DTO_TO_FLOW_PROCESSOR));
            Set<NifiFlowProcessor> sources = new HashSet<>(Collections2.transform(processor.getSources(), NIFI_PROCESSOR_DTO_TO_FLOW_PROCESSOR));
            Set<NifiFlowProcessor> failureProcessors = new HashSet<>(Collections2.transform(processor.getFailureProcessors(), PROCESSOR_DTO_TO_FLOW_PROCESSOR));
            flowProcessor.setIsFailure(processor.isFailureProcessor());
            flowProcessor.setIsEnd(processor.isEnd());
            flowProcessor.setSourceIds(sources.stream().map(source -> source.getId()).collect(Collectors.toSet()));
            flowProcessor.setDestinationIds(destinations.stream().map(dest -> dest.getId()).collect(Collectors.toSet()));
            flowProcessor.setSources(sources);
            flowProcessor.setDestinations(destinations);
            flowProcessor.setParentGroupId(processor.getDto().getParentGroupId());
            if (inspectForFailureRelationships) {
                flowProcessor.setFailureProcessors(failureProcessors);
            }
            flowProcessor.setSourceConnectionIds(processor.getSourceConnectionIdentifiers());
            flowProcessor.setDestinationConnectionIds(processor.getDestinationConnectionIdentifiers());

            return flowProcessor;
        }

    };


    /**
     * Convert a NifiVisitableProcessGroup to a simple one
     */
    private final Function<NifiVisitableProcessGroup, NifiFlowProcessGroup> NIFI_DTO_GROUP_TO_FLOW_GROUP = group -> {

        NifiFlowProcessGroup flowProcessGroup = PROCESS_GROUP_DTO_TO_FLOW_GROUP.apply(group.getDto());
        Set<NifiFlowProcessor> starting = new HashSet<>(Collections2.transform(group.getStartingProcessors(), NIFI_PROCESSOR_DTO_TO_FLOW_PROCESSOR));
        flowProcessGroup.setStartingProcessors(starting);
        return flowProcessGroup;
    };


    private NifiFlowProcessGroup toFlowProcessGroup(NifiVisitableProcessGroup group) {
        return NIFI_DTO_GROUP_TO_FLOW_GROUP.apply(group);
    }


}
