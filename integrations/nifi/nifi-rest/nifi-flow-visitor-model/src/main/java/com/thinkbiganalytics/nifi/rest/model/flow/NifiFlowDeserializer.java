package com.thinkbiganalytics.nifi.rest.model.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/13/16.
 *
 *
 *
 *
 * JSON doesnt like circular dependencies. NifiFlowProcessor has a graph of objects that point up and down the tree. All attempts to deserialize the object with Jackson annotations on the
 * NifiFlowProcessor failed For Example (@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")) Reference https://github.com/FasterXML/jackson-databind/issues/687 To
 * get around this ids are stored instead of the entire object in the graph. This class will reattach the real objects back so you can then traverse the graph.
 */
public class NifiFlowDeserializer {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowDeserializer.class);

    /**
     * Construct the graph by reattaching the source/dest by looking at the ids
     */
    public static void constructGraph(NifiFlowProcessGroup group) {
        if (group != null) {
            log.info("constructing graph for {} ({})", group.getName(), group.getId());
            //reattach the ids back to the graph of objects
            Map<String, NifiFlowProcessor> processorMap = group.getProcessorMap();

            processorMap.values().forEach(processor -> {
                Set<NifiFlowProcessor> sources = processor.getSourceIds().stream().map(sourceId -> processorMap.get(sourceId)).collect(Collectors.toSet());
                Set<NifiFlowProcessor> destinations = processor.getDestinationIds().stream().map(destId -> processorMap.get(destId)).collect(Collectors.toSet());
                Set<NifiFlowProcessor> failures = processor.getFailureProcessors().stream().map(processorId -> processorMap.get(processorId)).collect(Collectors.toSet());

                processor.setSources(sources);
                processor.setDestinations(destinations);
                processor.setFailureProcessors(failures);
                processor.setProcessGroup(group);
            });

            //reattach to the group
            group.setStartingProcessors(group.getStartingProcessors().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toSet()));

            group.setEndingProcessors(
                group.getEndingProcessors().values().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor)));

            group.setFailureProcessors(
                group.getFailureProcessors().values().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor)));

            Map<String, List<NifiFlowProcessor>> failureConnectionProcessors = new HashMap<>();

            if (group.getFailureConnectionIdToSourceProcessorMap() != null) {
                group.getFailureConnectionIdToSourceProcessorMap().entrySet().forEach(connectionIdProcessorIdEntry -> {
                    List<NifiFlowProcessor> populatedProcessors = connectionIdProcessorIdEntry.getValue().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toList());
                    failureConnectionProcessors.put(connectionIdProcessorIdEntry.getKey(), populatedProcessors);

                });
            }
            group.setFailureConnectionIdToSourceProcessorMap(failureConnectionProcessors);

        }
    }

    public static void prepareForSerialization(NifiFlowProcessGroup group) {
        //ensure the objects are cleared of references
        Map<String, NifiFlowProcessor> processorMap = group.getProcessorMap();

        processorMap.values().forEach(processor -> {
            processor.setSources(null);
            processor.setDestinations(null);
            processor.setFailureProcessors(null);
            processor.setProcessGroup(null);
        });

    }


}
