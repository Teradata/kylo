package com.thinkbiganalytics.nifi.rest.model.flow;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JSON doesnt like circular dependencies. NifiFlowProcessor has a graph of objects that point up and down the tree. All attempts to deserialize the object with Jackson annotations on the
 * NifiFlowProcessor failed For Example (@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")) Reference https://github.com/FasterXML/jackson-databind/issues/687 To
 * get around this ids are stored instead of the entire object in the graph. This class will reattach the real objects back so you can then traverse the graph.
 */
public class NifiFlowDeserializer {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowDeserializer.class);

    /**
     * Construct the graph by reattaching the source/dest by looking at the ids
     *
     * @param group the graph of the flow to deserialize and connect and convert the id references to objects
     */
    public static void constructGraph(NifiFlowProcessGroup group) {
        if (group != null) {
            log.info("constructing graph for {} ({})", group.getName(), group.getId());
            //reattach the ids back to the graph of objects
            Map<String, NifiFlowProcessor> processorMap = group.getProcessorMap();

            processorMap.values().forEach(processor -> {
                Set<NifiFlowProcessor> sources = processor.getSourceIds().stream().map(sourceId -> processorMap.get(sourceId)).collect(Collectors.toSet());
                Set<NifiFlowProcessor> destinations = processor.getDestinationIds().stream().map(destId -> processorMap.get(destId)).collect(Collectors.toSet());

                processor.setSources(sources);
                processor.setDestinations(destinations);
                processor.setProcessGroup(group);
            });

            //reattach to the group
            group.setStartingProcessors(group.getStartingProcessors().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toSet()));

            group.setEndingProcessors(
                group.getEndingProcessors().values().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor)));


        }
    }

    /**
     * Remove any circular graph dependencies and replace it with id references
     * These can be reconstructed later via the {@link #constructGraph(NifiFlowProcessGroup)}
     *
     * @param group the graph of the flow to serialize
     */
    public static void prepareForSerialization(NifiFlowProcessGroup group) {
        //ensure the objects are cleared of references
        Map<String, NifiFlowProcessor> processorMap = group.getProcessorMap();

        processorMap.values().forEach(processor -> {
            processor.setSources(null);
            processor.setDestinations(null);
            processor.setProcessGroup(null);
        });

    }


}
