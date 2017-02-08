package com.thinkbiganalytics.nifi.rest.model.visitor;

/*-
 * #%L
 * thinkbig-nifi-flow-visitor-model
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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Build the light weight {@link NifiFlowProcessor} and {@link NifiFlowProcessGroup} from the {@link NifiVisitableProcessor} and {@link NifiVisitableProcessGroup}
 */
public class NifiFlowBuilder {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowBuilder.class);
    /**
     * Convert a NiFi {@link ProcessorDTO} to a {@link NifiFlowProcessor}
     */
    private static final Function<ProcessorDTO, NifiFlowProcessor> PROCESSOR_DTO_TO_FLOW_PROCESSOR = new Function<ProcessorDTO, NifiFlowProcessor>() {
        @Override
        public NifiFlowProcessor apply(ProcessorDTO processor) {
            return new NifiFlowProcessor(processor.getId(), processor.getName(), processor.getType());
        }
    };
    /**
     * Convert a NiFi {@link ProcessGroupDTO} to a {@link NifiFlowProcessGroup}
     */
    private static final Function<ProcessGroupDTO, NifiFlowProcessGroup> PROCESS_GROUP_DTO_TO_FLOW_GROUP = new Function<ProcessGroupDTO, NifiFlowProcessGroup>() {
        @Override
        public NifiFlowProcessGroup apply(ProcessGroupDTO group) {
            return new NifiFlowProcessGroup(group.getId(), group.getName());
        }
    };
    Map<String, NifiFlowProcessor> cache = new ConcurrentHashMap<>();
    /**
     * Convert a {@link NifiVisitableProcessor} to a  simplified {@link NifiFlowProcessor}
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
            flowProcessor.setIsEnd(processor.isEnd());
            flowProcessor.setSourceIds(sources.stream().map(source -> source.getId()).collect(Collectors.toSet()));
            flowProcessor.setDestinationIds(destinations.stream().map(dest -> dest.getId()).collect(Collectors.toSet()));
            flowProcessor.setSources(sources);
            flowProcessor.setDestinations(destinations);
            flowProcessor.setParentGroupId(processor.getDto().getParentGroupId());
            flowProcessor.setSourceConnectionIds(processor.getSourceConnectionIdentifiers());
            flowProcessor.setDestinationConnectionIds(processor.getDestinationConnectionIdentifiers());

            return flowProcessor;
        }

    };
    /**
     * Convert a {@link NifiVisitableProcessGroup} to a  simplified {@link NifiFlowProcessGroup}
     */
    private final Function<NifiVisitableProcessGroup, NifiFlowProcessGroup> NIFI_DTO_GROUP_TO_FLOW_GROUP = group -> {

        NifiFlowProcessGroup flowProcessGroup = PROCESS_GROUP_DTO_TO_FLOW_GROUP.apply(group.getDto());
        Set<NifiFlowProcessor> starting = new HashSet<>(Collections2.transform(group.getStartingProcessors(), NIFI_PROCESSOR_DTO_TO_FLOW_PROCESSOR));
        flowProcessGroup.setStartingProcessors(starting);
        return flowProcessGroup;
    };

    /**
     * Build the {@link NifiFlowProcessGroup} from the visited {@link NifiVisitableProcessGroup} returning the simplified graph of objects that make up the flow
     *
     * @param group the visited process group and its flow connecting processors together
     * @return the simplified graph representing the flow starting with the supplied visited process group
     */
    public NifiFlowProcessGroup build(NifiVisitableProcessGroup group) {
        NifiFlowProcessGroup flowProcessGroup = toFlowProcessGroup(group);
        flowProcessGroup.setProcessorMap(cache);
        flowProcessGroup.addConnections(group.getConnections());

        ProcessGroupDTO groupDTO = group.getParentProcessGroup();
        if (groupDTO != null) {
            flowProcessGroup.setParentGroupId(groupDTO.getId());
            flowProcessGroup.setParentGroupName(groupDTO.getName());
        }

        flowProcessGroup.assignFlowIds();

        return flowProcessGroup;
    }


    /**
     * Transform  a {@link NifiVisitableProcessGroup} to a  simplified {@link NifiFlowProcessGroup}
     *
     * @return a simplified object representing the flow graph
     */
    private NifiFlowProcessGroup toFlowProcessGroup(NifiVisitableProcessGroup group) {
        return NIFI_DTO_GROUP_TO_FLOW_GROUP.apply(group);
    }
}
