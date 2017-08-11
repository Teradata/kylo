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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowProcessorConnection;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Visit a NiFi processor and connect it to other processors
 *
 * @see NifiFlowVisitor
 */
public class NifiVisitableProcessor implements NifiVisitable {

    private static final Logger log = LoggerFactory.getLogger(NifiVisitableProcessor.class);


    private Set<NifiVisitableProcessor> sources; //parents
    private Set<NifiVisitableProcessor> destinations; //children

    /**
     * The NiFi processor id
     **/
    private String id;

    private Set<NiFiFlowProcessorConnection> sourceConnectionIdentifiers;

    private Set<NiFiFlowProcessorConnection> destinationConnectionIdentifiers;

    private ProcessorDTO dto;


    public NifiVisitableProcessor(ProcessorDTO dto) {
        this.dto = dto;
        this.id = dto.getId();
    }

    /**
     * visit the processor with the supplied visitor
     *
     * @param nifiVisitor the visitor
     */
    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {
        nifiVisitor.visitProcessor(this);
    }

    /**
     * connect this processor to a incoming source processor
     *
     * @param dto the sorce processor
     */
    public void addSource(NifiVisitableProcessor dto) {
        getSources().add(dto);
    }

    /**
     * connect this processor to a outgoing destination
     *
     * @param dto the destination processor
     */
    public void addDestination(NifiVisitableProcessor dto) {
        getDestinations().add(dto);
    }

    /**
     * Return all processors that come directly before this processor
     *
     * @return the source processors connecting to this processor
     */
    public Set<NifiVisitableProcessor> getSources() {
        if (sources == null) {
            sources = new HashSet<>();
        }
        return sources;
    }

    /**
     * Return all the processors that connect after this processor
     *
     * @return the destination processors
     */
    public Set<NifiVisitableProcessor> getDestinations() {
        if (destinations == null) {
            destinations = new HashSet<>();
        }
        return destinations;
    }

    /**
     * Return the NiFi object representing this processor
     *
     * @return the nifi processor
     */
    public ProcessorDTO getDto() {
        return dto;
    }

    /**
     * set the NiFi processor
     *
     * @param dto the NiFi processor
     */
    public void setDto(ProcessorDTO dto) {
        this.dto = dto;
    }


    /**
     * Check to see if that this processor does not have any input/sources connected into it
     *
     * @return {@code true} if no processors are connected as sources to this processor, {@code false} if otherwise
     */
    public boolean isStart() {
        return !getDestinations().isEmpty() && getSources().isEmpty();
    }

    /**
     * Check to see this processor is a leaft node meaning that this processor does not have any output/destinations connected from it
     *
     * @return {@code true} if no processors are connected as outputs from this processor, {@code false} if otherwise
     */
    public boolean isEnd() {
        return !getSources().isEmpty() && getDestinations().isEmpty();
    }

    /**
     * Check to see if this processor Id matches another incoming processor
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NifiVisitableProcessor processor = (NifiVisitableProcessor) o;

        return id.equals(processor.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    /***
     * Print out the processor to log
     */
    public void print() {

        print(0);
    }

    /**
     * Print out the processor to the log
     */
    public void print(Integer level) {

        log.info(level + ". " + getDto().getName());
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());
        Integer nextLevel = level + 1;

        for (NifiVisitableProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                child.print(nextLevel);
                printed.add(child.getId());
            }
        }
    }

    /**
     * Check to see if this processor contains an incoming processor
     *
     * @param parent the processor to check for in this processors {@link #getDestinations()}
     * @return {@code true} if this processor already contains the incoming processor as its destination, {@code false} if not
     */
    public boolean containsDestination(NifiVisitableProcessor parent) {
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

    /**
     * Return all the processors including the destinations walking thr graph of the {@link NifiVisitableProcessor#getDestinations()}
     *
     * @return all the processors including the destinations walking thr graph of the {@link NifiVisitableProcessor#getDestinations()}
     */
    public Set<ProcessorDTO> getProcessors() {
        return getProcessors(null);
    }


    /**
     * Return all the processors including the destinations walking thr graph of the {@link NifiVisitableProcessor#getDestinations()}
     *
     * @param set of processors to inspect and retreive the NiFi processor dto from
     * @return all the processors including the destinations walking thr graph of the {@link NifiVisitableProcessor#getDestinations()}
     */
    public Set<ProcessorDTO> getProcessors(Set<NifiVisitableProcessor> set) {
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(this);
        for (NifiVisitableProcessor child : getDestinations()) {
            if (!set.contains(child)) {
                child.getProcessors(set);
            }
        }

        Set<ProcessorDTO> p = new HashSet<>();
        for (NifiVisitableProcessor x : set) {
            p.add(x.getDto());
        }
        return p;

    }

    /**
     * Return the id for this processor
     *
     * @return the id for the processor
     */
    public String getId() {
        return id;
    }

    /**
     * add a connection as a source connection
     *
     * @param conn the source connection to this processor
     */
    public void addSourceConnectionIdentifier(ConnectionDTO conn) {
        getSourceConnectionIdentifiers().add(new NiFiFlowProcessorConnection(conn.getId(), conn.getName(), conn.getSelectedRelationships()));
    }

    /**
     * add a connectionas a destination connection
     *
     * @param conn the destination connection from this processor
     */
    public void addDestinationConnectionIdentifier(ConnectionDTO conn) {
        NiFiFlowProcessorConnection destinationConnection = new NiFiFlowProcessorConnection(conn.getId(), conn.getName(), conn.getSelectedRelationships());
        getDestinationConnectionIdentifiers().add(destinationConnection);

    }


    /**
     * Return all connection ids marked as sources coming to this processor
     *
     * @return all connection ids marked as sources coming to this processor
     */
    public Set<NiFiFlowProcessorConnection> getSourceConnectionIdentifiers() {
        if (sourceConnectionIdentifiers == null) {
            sourceConnectionIdentifiers = new HashSet<>();
        }
        return sourceConnectionIdentifiers;
    }

    /**
     * Return all connection ids marked as destinations from this processor
     *
     * @return all connection ids marked as destinations from this processor
     */
    public Set<NiFiFlowProcessorConnection> getDestinationConnectionIdentifiers() {
        if (destinationConnectionIdentifiers == null) {
            destinationConnectionIdentifiers = new HashSet<>();
        }
        return destinationConnectionIdentifiers;
    }

}
