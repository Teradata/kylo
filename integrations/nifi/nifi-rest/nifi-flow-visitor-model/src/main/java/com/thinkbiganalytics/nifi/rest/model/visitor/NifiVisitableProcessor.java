package com.thinkbiganalytics.nifi.rest.model.visitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowProcessorConnection;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableProcessor implements  NifiVisitable {
    private static final Logger log = LoggerFactory.getLogger(NifiVisitableProcessor.class);


    private Set<NifiVisitableProcessor> sources; //parents
    private Set<NifiVisitableProcessor> destinations; //children

    private String id;

    private String inputPortId;

    private String outputPortId;

    private Map<String,NifiVisitableProcessor> inputPortIdProcessorMap;

    private Map<String,NifiVisitableProcessor> outputPortIdProcessorMap;

    private boolean isFailureProcessor;

    private Set<NiFiFlowProcessorConnection> sourceConnectionIdentifiers;

    private Set<NiFiFlowProcessorConnection> destinationConnectionIdentifiers;

    private ProcessorDTO dto;
    public NifiVisitableProcessor(ProcessorDTO dto) {
        this.dto = dto;
        this.id = dto.getId();
        this.inputPortIdProcessorMap = new HashMap<>();
        this.outputPortIdProcessorMap = new HashMap<>();
    }

    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {
       nifiVisitor.visitProcessor(this);
    }
    public void addSource(NifiVisitableProcessor dto){
        getSources().add(dto);
    }

    public void addDestination(NifiVisitableProcessor dto) {
        getDestinations().add(dto);
     }

    public Set<NifiVisitableProcessor> getSources() {
        if(sources == null){
            sources = new HashSet<>();
        }
        return sources;
    }

    public void setSources(Set<NifiVisitableProcessor> sources) {
        this.sources = sources;
    }

    public Set<NifiVisitableProcessor> getDestinations() {
        if(destinations == null){
            destinations = new HashSet<>();
        }
        return destinations;
    }

    public void setDestinations(Set<NifiVisitableProcessor> destinations) {
        this.destinations = destinations;
    }

    public ProcessorDTO getDto() {
        return dto;
    }

    public void setDto(ProcessorDTO dto) {
        this.dto = dto;
    }


    public boolean isStart(){
        return !getDestinations().isEmpty() && getSources().isEmpty();
    }

    public  boolean isEnd(){
        return !getSources().isEmpty() && getDestinations().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NifiVisitableProcessor processor = (NifiVisitableProcessor) o;

        return id.equals(processor.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }



    public void print(){

        print(0);
    }

    public void print(Integer level){

        log.info(level + ". " + getDto().getName());
        Set<String> printed= new HashSet<>();
        printed.add(this.getId());
        Integer nextLevel = level +1;

        for(NifiVisitableProcessor child: getDestinations()){
            if(!child.containsDestination(this) &&  !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())){
                child.print(nextLevel);
                printed.add(child.getId());
            }
        }
    }

    //used for connections with input/output ports

    public boolean containsDestination(NifiVisitableProcessor parent){
        final String thisId = getDto().getId();
        final String parentId = parent.getDto().getId();
        NifiVisitableProcessor p = Iterables.tryFind(getDestinations(), new Predicate<NifiVisitableProcessor>() {
            @Override
            public boolean apply(NifiVisitableProcessor nifiVisitableProcessor) {
                return nifiVisitableProcessor.getDto().getId().equalsIgnoreCase(thisId) || nifiVisitableProcessor.getDto().getId()
                    .equalsIgnoreCase(parentId);
            }
        }).orNull();
        return p != null;
    }



    public Set<ProcessorDTO> getFailureProcessors() {
        Set<ProcessorDTO> failureProcessors = new HashSet<>();
        Set<NifiVisitableProcessor> set = new HashSet<>();
        populateChildProcessors(set);
        for (NifiVisitableProcessor p : set) {
            if (p.isFailureProcessor()) {
                failureProcessors.add(p.getDto());
            }
        }
        return failureProcessors;
    }

    public Set<ProcessorDTO> getProcessors(){
       return getProcessors(null);
    }

    public void populateChildProcessors(Set<NifiVisitableProcessor> set) {
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(this);
        for (NifiVisitableProcessor child : getDestinations()) {
            if (!set.contains(child)) {
                child.getProcessors(set);
            }
        }
    }

    public Set<ProcessorDTO> getProcessors(Set<NifiVisitableProcessor> set){
          if(set == null) {
              set = new HashSet<>();
          }
          set.add(this);
        for (NifiVisitableProcessor child : getDestinations()) {
            if(!set.contains(child)) {
                child.getProcessors(set);
            }
        }

        Set<ProcessorDTO> p = new HashSet<>();
        for(NifiVisitableProcessor x: set){
            p.add(x.getDto());
        }
        return p;

    }


    public boolean isFailureProcessor() {
        return isFailureProcessor;
    }

    public void setIsFailureProcessor(boolean isFailureProcessor) {
        this.isFailureProcessor = isFailureProcessor;
    }

    public String getId() {
        return id;
    }

    public void addSourceConnectionIdentifier(ConnectionDTO conn) {
        getSourceConnectionIdentifiers().add(new NiFiFlowProcessorConnection(conn.getId(), conn.getName(), conn.getSelectedRelationships()));
    }

    public void addDestinationConnectionIdentifier(ConnectionDTO conn) {
        NiFiFlowProcessorConnection destinationConnection = new NiFiFlowProcessorConnection(conn.getId(), conn.getName(), conn.getSelectedRelationships());
        getDestinationConnectionIdentifiers().add(destinationConnection);

    }


    public Set<NiFiFlowProcessorConnection> getSourceConnectionIdentifiers() {
        if (sourceConnectionIdentifiers == null) {
            sourceConnectionIdentifiers = new HashSet<>();
        }
        return sourceConnectionIdentifiers;
    }

    public void setSourceConnectionIdentifiers(Set<NiFiFlowProcessorConnection> sourceConnectionIdentifiers) {
        this.sourceConnectionIdentifiers = sourceConnectionIdentifiers;
    }

    public Set<NiFiFlowProcessorConnection> getDestinationConnectionIdentifiers() {
        if (destinationConnectionIdentifiers == null) {
            destinationConnectionIdentifiers = new HashSet<>();
        }
        return destinationConnectionIdentifiers;
    }

    public void setDestinationConnectionIdentifiers(Set<NiFiFlowProcessorConnection> destinationConnectionIdentifiers) {
        this.destinationConnectionIdentifiers = destinationConnectionIdentifiers;
    }
}
