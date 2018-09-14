package com.thinkbiganalytics.feedmgr.service.template;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheBaseProcessorDTO;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * For a given template identify the input (first) processors in the flow adn create a map of the processor to all of its downstream processors
 */
public class NiFiTemplateCacheProcessorGraph {


    enum KEY {
        ID,NAME;
    }
    /**
     * The key to be used for the map of input processor, to List of downstream processors
     * Defaults to using the Name since the template processor ids will vary
     */
    private KEY key = KEY.NAME;

    private static final Logger log = LoggerFactory.getLogger(NiFiTemplateCacheProcessorGraph.class);

    /**
     * Map of the input processor id to all of its downstream processors
     */
   private Map<String,List<NifiFlowCacheBaseProcessorDTO>> inputProcessorRelations = new ConcurrentHashMap<>();

    /**
     * map of the source to destinations entities
     */
    private Map<String,List<String>> connectionSourceToDestination = new ConcurrentHashMap<>();

    /**
     * When mapping processor connections templates can connect various things between each processor (i.e. processorA - port - processgroup - port -processorB,  processor - funnel - processor)
     * When doing this if the dest is of something other than a processor, map the source processor to that thing (i.e. port, funnel) and mark it as an alias for that processor so eventually it
     * can make the connection between the two processors (i.e. processorA - processorB)
     */
    private Map<String,List<String>>connectionSourceProcessorAliases = new ConcurrentHashMap<>();


    /**
     * Map of all entities that are connected to other enties, both of which are not processors
     */
    private Map<String,List<String>> nonProcessorEntityRelations  = new ConcurrentHashMap<>();

    /**
     * Map of the processor id to its respective name
     */
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();

    TemplateDTO template;
    public NiFiTemplateCacheProcessorGraph(TemplateDTO template){
        this.template = template;
        this.key = KEY.NAME;
    }


    /**
     * Return the map of Processor Name or ID (defaults to Name based upon this.key) to the list of downstream processors
     * @return
     */
    public Map<String,List<NifiFlowCacheBaseProcessorDTO>> build(){

        List<ConnectionDTO> connectionDTOS = new ArrayList<>();
        Set<ProcessorDTO> processorDTOS = NifiProcessUtil.getProcessors(template);
        template.getSnippet().getProcessGroups().stream().forEach(group -> {
            connectionDTOS.addAll(NifiConnectionUtil.getAllConnections(group));
        });
        connectionDTOS.addAll(template.getSnippet().getConnections());


      processorDTOS.stream().forEach(p-> {
          processorIdToProcessorName.put(p.getId(),p.getName());
      });

        addConnectionSourceToDestination(connectionDTOS);

        List<String> inputProcessorIds = NifiConnectionUtil.getInputProcessorIds(connectionDTOS);
        populateInputProcessorRelations(inputProcessorIds);

        return inputProcessorRelations;
    }




    /**
     * recursively add and connect processors
     * @param processorId
     * @param connectionSourceToDestination
     * @return
     */
    private List<NifiFlowCacheBaseProcessorDTO>  getProcessorDestinations(String processorId, Map<String,List<String>> connectionSourceToDestination, Map<String,NifiFlowCacheBaseProcessorDTO> processedDestinations){
        if(processedDestinations == null){
            processedDestinations = new HashMap<>();
        }
        List<NifiFlowCacheBaseProcessorDTO> destinations = new ArrayList<>();
        List<String> destIds = connectionSourceToDestination.get(processorId);
        if(destIds != null ) {
            for (String id : destIds) {
                if (processedDestinations.containsKey(id)) {
                    destinations.add(processedDestinations.get(id));
                } else if (id != processorId) {
                    String processorName = processorIdToProcessorName.get(id);
                    if (StringUtils.isNotBlank(processorName)) {
                        NifiFlowCacheBaseProcessorDTO dto = new NifiFlowCacheBaseProcessorDTO();
                        dto.setId(id);
                        dto.setName(processorName);
                        destinations.add(dto);
                        processedDestinations.put(id, dto);
                        List<NifiFlowCacheBaseProcessorDTO> destProcessors = getProcessorDestinations(id, connectionSourceToDestination, processedDestinations);
                        destinations.addAll(destProcessors);
                    }
                } else {
                    log.info("skipping processor relation {}", id);
                }
            }
        }
        return destinations;
    }

    private void populateInputProcessorRelations(List<String> inputProcessors){
        inputProcessors.stream().forEach(processorId -> {
            List destinationList = getProcessorDestinations(processorId,connectionSourceToDestination,null);
            if(this.key == KEY.ID) {
                inputProcessorRelations.put(processorId, destinationList);
            }
            else {
                String name = processorIdToProcessorName.get(processorId);
                inputProcessorRelations.put(name, destinationList);
            }
        });
    }






    /**
     * populate the maps needed to connect non processor entities back to their respective processors
     * This is used to determine the graph of connected processors for a feed
     * @param connectionMap
     */
    private void buildNonProcessorSourceDestinationRelationships(List<ConnectionDTO> connections) {
        connections.stream().forEach(c -> {
            String source = c.getSource() != null ? c.getSource().getId(): null;
            String dest = c.getDestination() != null ? c.getDestination().getId() : null;
            if (StringUtils.isNotBlank(dest) && StringUtils.isNotBlank(source)) {
                boolean isSourceProcessor = processorIdToProcessorName.containsKey(source);
                boolean isDestProcessor = processorIdToProcessorName.containsKey(dest);
                if (!isSourceProcessor && !isDestProcessor) {
                    //relate these two together
                    log.info("Found 2 non processors connected {} to {} ", source, dest);
                    nonProcessorEntityRelations.computeIfAbsent(source, src -> new ArrayList<>()).add(dest);
                    nonProcessorEntityRelations.computeIfAbsent(dest, src -> new ArrayList<>()).add(source);
                } else if (isSourceProcessor && !isDestProcessor) {
                    connectionSourceProcessorAliases.computeIfAbsent(dest, src -> new ArrayList<String>()).add(source);

                } else if (!isSourceProcessor && isDestProcessor) {
                    connectionSourceProcessorAliases.computeIfAbsent(source, src -> new ArrayList<String>()).add(dest);
                }
            }
        });
    }


    private void addConnectionSourceToDestination(List<ConnectionDTO> connections){

        //map any connections that are not directly related to a processor to the other hashmaps to help determine the true processor -> processor references
        buildNonProcessorSourceDestinationRelationships(connections);
        connections.stream().forEach(c -> {
            String source = c.getSource() != null ? c.getSource().getId(): null;
            String dest = c.getDestination() != null ? c.getDestination().getId() : null;
            if (StringUtils.isNotBlank(dest) && StringUtils.isNotBlank(source)) {
                boolean isSourceProcessor = processorIdToProcessorName.containsKey(source);
                boolean isDestProcessor = processorIdToProcessorName.containsKey(dest);
                if(!isSourceProcessor) {
                    //find the source
                    String sourceProcessor = null;
                    if(nonProcessorEntityRelations.containsKey(source)){
                        sourceProcessor = nonProcessorEntityRelations.get(source).stream().filter(id -> connectionSourceProcessorAliases.containsKey(id)).map(id -> {
                            return connectionSourceProcessorAliases.get(id).get(0);

                        }).findFirst().orElse(null);
                    }

                    if(sourceProcessor == null){
                        sourceProcessor = connectionSourceProcessorAliases.containsKey(source) ? connectionSourceProcessorAliases.get(source).get(0) : null;
                    }

                    if(sourceProcessor != null){
                        log.info("Found source through relationship connected {} to {} with other source as {}  ",sourceProcessor,dest, source);
                        source = sourceProcessor;
                    }
                    isSourceProcessor = true;
                }

                if(!isDestProcessor){

                    String destProcessor = null;
                    if(nonProcessorEntityRelations.containsKey(dest)){
                        destProcessor = nonProcessorEntityRelations.get(dest).stream().filter(id -> connectionSourceProcessorAliases.containsKey(id)).map(id -> {
                            return connectionSourceProcessorAliases.get(id).get(0);

                        }).findFirst().orElse(null);
                    }
                    if(destProcessor == null){
                        destProcessor = connectionSourceProcessorAliases.containsKey(dest) ? connectionSourceProcessorAliases.get(dest).get(0) : null;
                    }

                    if(destProcessor != null){
                        log.info("Found dest through relationship connected {} to {} with other dest as {}  ",source,destProcessor, dest);
                        dest = destProcessor;

                    }
                    isDestProcessor = true;
                }

                if(isSourceProcessor && isDestProcessor && !source.equalsIgnoreCase(dest)) {
                    connectionSourceToDestination.computeIfAbsent(source,src ->new ArrayList<String>()).add(dest);
                }



            }
        });

    }



}
