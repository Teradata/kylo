package com.thinkbiganalytics.nifi.rest.model.visitor;


import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiConnectionOrderVisitor implements NifiFlowVisitor {

    private NifiVisitableProcessGroup currentProcessGroup;

    private NifiVisitableProcessGroup processGroup;

    private Map<String,ProcessorDTO> processorsMap = new HashMap<>();

    private Map<String,NifiVisitableProcessor> visitedProcessors = new HashMap<>();


    private Map<String,NifiVisitableProcessGroup> visitedProcessGroups = new HashMap<>();



    public NifiConnectionOrderVisitor(NifiVisitableProcessGroup processGroup) {
     this.processGroup = processGroup;
        this.currentProcessGroup = processGroup;
     this.processorsMap =  NifiProcessUtil.getProcessorsMap(processGroup.getDto());
    }

    @Override
    public void visitProcessor(NifiVisitableProcessor processor) {
        visitedProcessors.put(processor.getDto().getId(), processor);
        //add the pointer to the ProcessGroup
        currentProcessGroup.addProcessor(processor);
    }

    @Override
    public void visitConnection(NifiVisitableConnection connection) {
       Set<String> relationships = connection.getDto().getSelectedRelationships();
       String sourceType = connection.getDto().getSource().getType();

        NifiVisitableProcessor destinationProcessor = getDestinationProcessor(connection.getDto());
        NifiVisitableProcessor sourceProcessor = getSourceProcessor(connection.getDto());

        if(destinationProcessor != null){
           if(relationships != null && relationships.contains("failure") && !relationships.contains("success")){
               destinationProcessor.setIsFailureProcessor(true);
           }
        }
        if(destinationProcessor != null && sourceProcessor != null){
            destinationProcessor.addSource(sourceProcessor);
            //REMOVE
            sourceProcessor.addDestination(destinationProcessor);
        }
        else {
            int i=0;
        }

    }

    @Override
    public void visitProcessGroup(NifiVisitableProcessGroup processGroup) {


        NifiVisitableProcessGroup group = visitedProcessGroups.get(processGroup.getDto().getId());

        if(group == null) {
            group = processGroup;
        }
        this.currentProcessGroup = group;
        group.accept(this);
        //group.populateStartingAndEndingProcessors();
        this.visitedProcessGroups.put(group.getDto().getId(), group);

    }

    /**
     * Return the Destination Processor going through the input ports/output ports
     * @param connection
     * @return
     */
    public NifiVisitableProcessor getDestinationProcessor(ConnectionDTO connection) {
        ConnectableDTO dest = connection.getDestination();
        NifiVisitableProcessor destinationProcessor = null;
        if(dest != null){


            if("INPUT_PORT".equalsIgnoreCase(dest.getType())) {
                NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
                destinationProcessor = group.getInputPortProcessor();
                destinationProcessor.setInputPortId(dest.getId());
            }
            else if("OUTPUT_PORT".equals(dest.getType())) {
                //get parent processgroup connection to input port
                NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
                if(group == null) {
                    group = processGroup;
                }
                //get parents process group processor that is connected to this id
                NifiVisitableProcessGroup parent = visitedProcessGroups.get(group.getDto().getParentGroupId());
                //if the parent is null the parent is the starting process group
                if(parent == null){
                    parent = processGroup;
                }

                ConnectionDTO conn = parent.getConnectionMatchingSourceId(dest.getId());
                //get the processor whos source matches this connection Id
                destinationProcessor = getDestinationProcessor(conn);
                if(destinationProcessor != null) {
                    destinationProcessor.setOutputPortId(dest.getId());
                }
                NifiVisitableProcessor outputProcessor = getSourceProcessor(connection);
                outputProcessor.setOutputPortId(dest.getId());
                currentProcessGroup.setOutputPortProcessor(outputProcessor);
            }
            else if("PROCESSOR".equals(dest.getType()))
            {
                destinationProcessor = getConnectionProcessor(dest.getId());
            }
        }
        return destinationProcessor;
    }

    public NifiVisitableProcessor getSourceProcessor(ConnectionDTO connection) {

        ConnectableDTO source = connection.getSource();
        NifiVisitableProcessor sourceProcessor = null;
        if(source != null) {
            if ("INPUT_PORT".equalsIgnoreCase(source.getType())) {
                NifiVisitableProcessGroup group = visitedProcessGroups.get(source.getGroupId());
                if(group == null) {
                    group = processGroup;
                }
                NifiVisitableProcessGroup parent = visitedProcessGroups.get(group.getDto().getParentGroupId());
                //if the parent is null the parent is the starting process group
                if(parent == null) {
                    parent = processGroup;
                }

                ConnectionDTO conn = parent.getConnectionMatchingDestinationId(source.getId());

                //get the processor whos source matches this connection Id
                sourceProcessor = getSourceProcessor(conn);
                //assign the inputPortProcessor == the the destination of this connection

                NifiVisitableProcessor inputProcessor = getDestinationProcessor(connection);
                inputProcessor.setInputPortId(source.getId());
                currentProcessGroup.setInputPortProcessor(inputProcessor);

            } else if ("OUTPUT_PORT".equals(source.getType())) {
                //get the sources group id then get the ending processor for that group
                NifiVisitableProcessGroup group = visitedProcessGroups.get(source.getGroupId());
                sourceProcessor = group.getOutputPortProcessor();
                sourceProcessor.setOutputPortId(source.getId());
            } else if ("PROCESSOR".equals(source.getType())) {
                sourceProcessor = getConnectionProcessor(source.getId());
            }

        }
        return sourceProcessor;
    }



   private NifiVisitableProcessor getConnectionProcessor(String id) {
       NifiVisitableProcessor processor = visitedProcessors.get(id);
           if (processor == null) {
               processor = new NifiVisitableProcessor(this.processorsMap.get(id));
               visitedProcessors.put(id, processor);
               //add the pointer to the ProcessGroup
               currentProcessGroup.addProcessor(processor);
           }
       return processor;
    }



    public Integer getNumberOfSplits(){
        int count = 0;
        for(NifiVisitableProcessor processor : visitedProcessors.values()) {
            Set<NifiVisitableProcessor> destinations = processor.getDestinations();
            if(destinations != null && !destinations.isEmpty()){
                count += (destinations.size() -1);
            }
        }
        return count;
    }

    /**
     * inspect the current status and determine if it has data in queue
     * @return
     */
    public boolean isProcessingData(){
        return false;
    }

    public void printOrder() {
        for(NifiVisitableProcessor processor: processGroup.getStartingProcessors()) {
            processor.print();
        }
    }


}
