package com.thinkbiganalytics.nifi.rest.model.visitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableProcessGroup implements  NifiVisitable {

    private ProcessGroupDTO dto;

    private Set<NifiVisitableProcessor> startingProcessors;

    private Set<NifiVisitableProcessor> endingProcessors;

    public Set<NifiVisitableProcessor> processors;


    private NifiVisitableProcessor inputPortProcessor;

    private NifiVisitableProcessor outputPortProcessor;

    private Map<String,NifiVisitableProcessor> outputPortProcessors;
    private Map<String,NifiVisitableProcessor> inputPortProcessors;

    private Set<ConnectionDTO> connections;

    public NifiVisitableProcessGroup(ProcessGroupDTO dto) {
        this.dto = dto;
        startingProcessors = new HashSet<>();
        endingProcessors = new HashSet<>();
        outputPortProcessors = new HashMap<>();
        inputPortProcessors = new HashMap<>();
        processors = new HashSet<>();
        if(dto.getContents() != null) {
            this.connections = dto.getContents().getConnections();
        }

    }

    private NifiVisitableProcessor getOrCreateProcessor(NifiFlowVisitor nifiVisitor, ProcessorDTO processorDTO) {
        NifiVisitableProcessor processor = nifiVisitor.getProcessor(processorDTO.getId());
        if(processor == null){
            processor = new NifiVisitableProcessor(processorDTO);
            addProcessor(processor);
        }
        return processor;
    }
    private NifiVisitableProcessGroup getOrCreateProcessGroup(NifiFlowVisitor nifiVisitor, ProcessGroupDTO processGroupDTO) {
        NifiVisitableProcessGroup group = nifiVisitor.getProcessGroup(processGroupDTO.getId());
        if(group == null) {
            group = new NifiVisitableProcessGroup(processGroupDTO);
        }
        return group;
    }


    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {
        if(dto.getContents()!= null) {
            //GET DATA IN THIS ORDER
            //1. Get Processor Info
            //2. get Process Group info
            //3. get Connections and make relationships between processors

            if (dto.getContents().getProcessors() != null) {
                for (ProcessorDTO processorDTO : dto.getContents().getProcessors()) {
                    NifiVisitableProcessor processor =   getOrCreateProcessor(nifiVisitor,processorDTO);
                    nifiVisitor.visitProcessor(processor);
                }
            }

            if (dto.getContents().getProcessGroups() != null) {
                for (ProcessGroupDTO processGroupDTO : dto.getContents().getProcessGroups()) {
                    if (processGroupDTO != null) {
                        nifiVisitor.visitProcessGroup(getOrCreateProcessGroup(nifiVisitor,processGroupDTO));
                    }
                }
            }

            if (dto.getContents().getConnections() != null) {
                for (ConnectionDTO connectionDTO : dto.getContents().getConnections()) {
                    nifiVisitor.visitConnection(new NifiVisitableConnection(this, connectionDTO));
                }
            }

            populateStartingAndEndingProcessors();
        }

    }

    public ProcessGroupDTO getDto() {
        return dto;
    }


    public void addProcessor(NifiVisitableProcessor processor){
        processors.add(processor);
    }

    public Set<NifiVisitableProcessor> getStartingProcessors() {
        return startingProcessors;
    }

    public Set<NifiVisitableProcessor> getEndingProcessors() {
        return endingProcessors;
    }

    public NifiVisitableProcessor getProcessorMatchingId(final String processorId){
        return Iterables.tryFind(processors, new Predicate<NifiVisitableProcessor>() {
            @Override
            public boolean apply(NifiVisitableProcessor processor) {
                return processor.getDto().getId().equals(processorId);
            }
        }).orNull();
    }

    public boolean containsProcessor(String processorId){
        return getProcessorMatchingId(processorId) != null;
    }

    public ConnectionDTO getConnectionMatchingSourceId(final String connectionId){
        if(connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getSource() != null && connection.getSource().getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }

    public ConnectionDTO getConnectionMatchingDestinationId(final String connectionId){
        if(connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getDestination() != null && connection.getDestination().getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }


    public ConnectionDTO getConnectionMatchingId(final String connectionId){
        if(connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }

    private void populateStartingAndEndingProcessors(){

        for(NifiVisitableProcessor processor : processors) {
            if(processor.isStart()){
                startingProcessors.add(processor);
            }
            if(processor.isEnd()){
                endingProcessors.add(processor);
            }
        }

    }


    public NifiVisitableProcessor getInputPortProcessor(String inputProcessorId) {
        return inputPortProcessors.get(inputProcessorId);
    }

    public void addInputPortProcessor(String id,NifiVisitableProcessor inputPortProcessor) {
        this.inputPortProcessors.put(id, inputPortProcessor);
    }

    public NifiVisitableProcessor getOutputPortProcessor(String connectionSourceId) {
        return outputPortProcessors.get(connectionSourceId);
    }

    public void addOutputPortProcessor(String connectionDestId,NifiVisitableProcessor outputPortProcessor) {
        this.outputPortProcessors.put(connectionDestId, outputPortProcessor);
    }

}
