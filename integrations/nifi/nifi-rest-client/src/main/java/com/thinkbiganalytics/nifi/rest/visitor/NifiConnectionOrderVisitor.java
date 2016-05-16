package com.thinkbiganalytics.nifi.rest.visitor;


import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowVisitor;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableConnection;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiConnectionOrderVisitor implements NifiFlowVisitor {
  private static final Logger log = LoggerFactory.getLogger(NifiConnectionOrderVisitor.class);

    private NifiVisitableProcessGroup currentProcessGroup;

    private NifiVisitableProcessGroup processGroup;

    private Map<String,ProcessorDTO> processorsMap = new HashMap<>();

    private Map<String,NifiVisitableProcessor> visitedProcessors = new HashMap<>();


    private Map<String,NifiVisitableProcessGroup> visitedProcessGroups = new HashMap<>();

    private NifiRestClient restClient;





    public NifiConnectionOrderVisitor(NifiRestClient restClient, NifiVisitableProcessGroup processGroup) {
        this.restClient = restClient;
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

        NifiVisitableProcessor destinationProcessor = getDestinationProcessor(connection.getDto(),true);

        NifiVisitableProcessor sourceProcessor = getSourceProcessor(connection.getDto());

        if(destinationProcessor != null){
           if(relationships != null && relationships.contains("failure") && !relationships.contains("success") && (StringUtils.isBlank(connection.getDto().getName()) || !StringUtils.startsWithIgnoreCase(connection.getDto().getName(),"retry"))){
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

    log.info(" Visit Process Group: {}, ({}) ",processGroup.getDto().getName(),processGroup.getDto().getId());

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
    public NifiVisitableProcessor getDestinationProcessor(ConnectionDTO connection, boolean getSource) {
        ConnectableDTO dest = connection.getDestination();
        NifiVisitableProcessor destinationProcessor = null;
        if(dest != null){


            if("INPUT_PORT".equalsIgnoreCase(dest.getType())) {
              boolean isNew = false;
                NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
              if (group == null) {
                group = fetchProcessGroup(dest.getGroupId());
              }
              ConnectionDTO conn = group.getConnectionMatchingSourceId(dest.getId());
              if(conn != null) {
                destinationProcessor = getDestinationProcessor(conn,getSource);
                if(destinationProcessor != null) {
                  destinationProcessor.setOutputPortId(dest.getId());
                  //add this processor to the visitor

                }
                if(getSource) {
                  NifiVisitableProcessor outputProcessor = getSourceProcessor(connection);
                  if(outputProcessor != null) {
                    outputProcessor.setOutputPortId(dest.getId());
                    currentProcessGroup.setOutputPortProcessor(outputProcessor);
                  }
                }
                //if(!visitedProcessGroups.containsKey(group.getDto().getId())){
                //  visitProcessGroup(group);
               // }
              }


            }
            else if("OUTPUT_PORT".equals(dest.getType())) {
              boolean isNew = false;
              //get parent processgroup connection to input port
              NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
              if (group == null) {
                group = fetchProcessGroup(dest.getGroupId());
              }
              ConnectionDTO conn = group.getConnectionMatchingSourceId(dest.getId());
              if(conn == null) {
                conn = searchConnectionMatchingSource(group.getDto().getParentGroupId(),dest.getId());
              }
              if (conn != null) {
                //get the processor whos source matches this connection Id
                destinationProcessor = getDestinationProcessor(conn,getSource);
                if (destinationProcessor != null) {
                  destinationProcessor.setOutputPortId(dest.getId());
                }
                if(getSource) {
                  NifiVisitableProcessor outputProcessor = getSourceProcessor(connection);
                  if(outputProcessor != null) {
                    outputProcessor.setOutputPortId(dest.getId());
                    currentProcessGroup.setOutputPortProcessor(outputProcessor);
                  }
                }
               // if(isNew){
              //    visitProcessGroup(group);
            //    }
              }
            }
            else if("PROCESSOR".equals(dest.getType()))
            {
                destinationProcessor = getConnectionProcessor(dest.getGroupId(),dest.getId());
            }
        }
        return destinationProcessor;
    }

  private NifiVisitableProcessGroup fetchProcessGroup(String groupId) {
    NifiVisitableProcessGroup group = processGroup;
    //fetch it
    try {
      ProcessGroupEntity processGroupEntity = restClient.getProcessGroup(groupId, false, true);
      //if the parent is null the parent is the starting process group
      if (processGroupEntity != null) {
        group = new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup());
      }
    } catch (JerseyClientException e) {
      e.printStackTrace();
    }
    return group;
  }




  private ConnectionDTO searchConnectionMatchingSource(String parentGroupId, String destinationId){
    //search up to find the connectoin that matches this dest id

    try {
      ProcessGroupEntity parent =  restClient.getProcessGroup(parentGroupId,false,true);
      if(parent != null) {
        //processGroup.getDto().setParent(parentParent.getProcessGroup());
        //get Contents of this parent
        NifiVisitableProcessGroup visitableProcessGroup = new NifiVisitableProcessGroup(parent.getProcessGroup());
        ConnectionDTO conn = visitableProcessGroup.getConnectionMatchingSourceId(destinationId);
        if(conn != null){
          return conn;
        }
      if (conn == null && parent.getProcessGroup().getParentGroupId() != null) {
         return searchConnectionMatchingSource(parent.getProcessGroup().getParentGroupId(),destinationId);
        }
      }

    } catch (JerseyClientException e) {
      e.printStackTrace();
    }
    return null;

  }

  private ConnectionDTO searchConnectionMatchingDestination(String parentGroupId, String sourceId){
    //search up to find the connectoin that matches this dest id

    try {
      ProcessGroupEntity parent =  restClient.getProcessGroup(parentGroupId,false,true);
      if(parent != null) {
        //processGroup.getDto().setParent(parentParent.getProcessGroup());
        //get Contents of this parent
        NifiVisitableProcessGroup visitableProcessGroup = new NifiVisitableProcessGroup(parent.getProcessGroup());
        ConnectionDTO conn = visitableProcessGroup.getConnectionMatchingDestinationId(sourceId);
        if(conn != null){
          return conn;
        }
        if (conn == null && parent.getProcessGroup().getParentGroupId() != null) {
          return searchConnectionMatchingSource(parent.getProcessGroup().getParentGroupId(),sourceId);
        }
      }

    } catch (JerseyClientException e) {
      e.printStackTrace();
    }
    return null;

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
                if(conn != null && conn != connection) {
                  //get the processor whos source matches this connection Id
                  sourceProcessor = getSourceProcessor(conn);
                  //assign the inputPortProcessor == the the destination of this connection
                }
                NifiVisitableProcessor inputProcessor = getDestinationProcessor(connection,false);
                inputProcessor.setInputPortId(source.getId());
                currentProcessGroup.setInputPortProcessor(inputProcessor);

            } else if ("OUTPUT_PORT".equals(source.getType())) {
                //get the sources group id then get the ending processor for that group
                NifiVisitableProcessGroup group = visitedProcessGroups.get(source.getGroupId());
              if(group == null){
                try {
                  ProcessGroupEntity processGroupEntity = restClient.getProcessGroup(source.getGroupId(), false, true);
              //    visitProcessGroup(new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup()));
                }catch (JerseyClientException e){
                  e.printStackTrace();
                }
              }
              if(group != null) {
                sourceProcessor = group.getOutputPortProcessor();
                sourceProcessor.setOutputPortId(source.getId());
              }
            } else if ("PROCESSOR".equals(source.getType())) {
                sourceProcessor = getConnectionProcessor(source.getGroupId(),source.getId());
            }

        }
        return sourceProcessor;
    }



   private NifiVisitableProcessor getConnectionProcessor(String groupId, String id) {
       NifiVisitableProcessor processor = visitedProcessors.get(id);
           if (processor == null) {
             if(!this.processorsMap.containsKey(id)){
               //if the current group is not related to this processgroup then attempt to walk this processors processgroup
               try {
                 ProcessGroupEntity processGroupEntity = restClient.getProcessGroup(groupId, false, true);
                 ProcessorDTO processorDTO = NifiProcessUtil.findFirstProcessorsById(
                     processGroupEntity.getProcessGroup().getContents().getProcessors(),id);
                 if(processorDTO != null) {
                   this.processorsMap.put(id,processorDTO);
                 }
                 if(processGroup.getDto().getId() != groupId && !visitedProcessGroups.containsKey(processGroupEntity.getProcessGroup().getId())) {
                     visitProcessGroup(new NifiVisitableProcessGroup(processGroupEntity.getProcessGroup()));
                   }
               } catch (JerseyClientException e) {
                 e.printStackTrace();
               }
             }
             //
               processor = visitedProcessors.get(id);
             if(processor == null){
               processor = new NifiVisitableProcessor(this.processorsMap.get(id));
               visitProcessor(processor);
             }


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
