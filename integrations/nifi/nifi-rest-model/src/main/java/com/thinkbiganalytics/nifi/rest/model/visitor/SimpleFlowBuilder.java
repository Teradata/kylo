package com.thinkbiganalytics.nifi.rest.model.visitor;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.nifi.rest.model.flow.SimpleNifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.SimpleNifiFlowProcessor;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * Created by sr186054 on 8/11/16.
 */
public class SimpleFlowBuilder {

    Map<String, SimpleNifiFlowProcessor> cache = new ConcurrentHashMap<>();

    public SimpleNifiFlowProcessGroup build(NifiVisitableProcessGroup group) {
        SimpleNifiFlowProcessGroup simpleGroup = toSimpleProcessGroup(group);
        simpleGroup.setProcessorMap(cache);
        ProcessGroupDTO groupDTO = group.getParentProcessGroup();
        if (groupDTO != null) {
            simpleGroup.setParentGroupId(groupDTO.getId());
            simpleGroup.setParentGroupName(groupDTO.getName());
        }
        return simpleGroup;
    }


    private static final Function<ProcessorDTO, SimpleNifiFlowProcessor> PROCESSOR_DTO_TO_SIMPLE = new Function<ProcessorDTO, SimpleNifiFlowProcessor>() {
        @Override
        public SimpleNifiFlowProcessor apply(ProcessorDTO processor) {
            return new SimpleNifiFlowProcessor(processor.getId(), processor.getName());
        }
    };

    private static final Function<ProcessGroupDTO, SimpleNifiFlowProcessGroup> PROCESS_GROUP_DTO_TO_SIMPLE = new Function<ProcessGroupDTO, SimpleNifiFlowProcessGroup>() {
        @Override
        public SimpleNifiFlowProcessGroup apply(ProcessGroupDTO group) {
            return new SimpleNifiFlowProcessGroup(group.getId(), group.getName());
        }
    };

    /**
     * Convert a NifiVisitableProcessor to a Simple one
     */
    private final Function<NifiVisitableProcessor, SimpleNifiFlowProcessor> COMPLEX_TO_SIMPLE_PROCESSOR = new Function<NifiVisitableProcessor, SimpleNifiFlowProcessor>() {
        @Nullable
        @Override
        public SimpleNifiFlowProcessor apply(NifiVisitableProcessor processor) {
            SimpleNifiFlowProcessor simple = null;
            if (cache.containsKey(processor.getId())) {
                return cache.get(processor.getId());
            }
            simple = PROCESSOR_DTO_TO_SIMPLE.apply(processor.getDto());
            cache.put(processor.getId(), simple);
            Set<SimpleNifiFlowProcessor> destinations = new HashSet<>(Collections2.transform(processor.getDestinations(), COMPLEX_TO_SIMPLE_PROCESSOR));
            Set<SimpleNifiFlowProcessor> sources = new HashSet<>(Collections2.transform(processor.getSources(), COMPLEX_TO_SIMPLE_PROCESSOR));
            Set<SimpleNifiFlowProcessor> failureProcessors = new HashSet<>(Collections2.transform(processor.getFailureProcessors(), PROCESSOR_DTO_TO_SIMPLE));
            simple.setIsFailure(processor.isFailureProcessor());
            simple.setIsEnd(processor.isEnd());

            simple.setSources(sources);
            simple.setDestinations(destinations);
            simple.setFailureProcessors(failureProcessors);
            return simple;
        }

    };


    /**
     * Convert a NifiVisitableProcessGroup to a simple one
     */
    private final Function<NifiVisitableProcessGroup, SimpleNifiFlowProcessGroup> COMPLEX_TO_SIMPLE_GROUP = group -> {

        SimpleNifiFlowProcessGroup simple = PROCESS_GROUP_DTO_TO_SIMPLE.apply(group.getDto());
        Set<SimpleNifiFlowProcessor> starting = new HashSet<>(Collections2.transform(group.getStartingProcessors(), COMPLEX_TO_SIMPLE_PROCESSOR));
        simple.setStartingProcessors(starting);
        return simple;
    };


    private SimpleNifiFlowProcessor toSimpleProcessor(NifiVisitableProcessor processor) {

        return COMPLEX_TO_SIMPLE_PROCESSOR.apply(processor);

    }

    private SimpleNifiFlowProcessGroup toSimpleProcessGroup(NifiVisitableProcessGroup group) {
        return COMPLEX_TO_SIMPLE_GROUP.apply(group);
    }

}
