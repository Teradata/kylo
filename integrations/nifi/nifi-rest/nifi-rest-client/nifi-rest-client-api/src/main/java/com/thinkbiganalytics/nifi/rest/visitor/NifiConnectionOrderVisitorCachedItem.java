package com.thinkbiganalytics.nifi.rest.visitor;

import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableConnection;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;

import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 12/25/16.
 */
public class NifiConnectionOrderVisitorCachedItem {

    private NifiVisitableProcessGroup processGroup;

    private Map<String, ProcessorDTO> processorsMap = new HashMap<>();

    private Map<String, NifiVisitableProcessor> visitedProcessors = new HashMap<>();


    private Map<String, NifiVisitableProcessGroup> visitedProcessGroups = new HashMap<>();

    private Set<NifiVisitableConnection> allConnections = new HashSet<>();


    public NifiConnectionOrderVisitorCachedItem(NifiConnectionOrderVisitor connectionOrderVisitor) {
        this.processGroup = connectionOrderVisitor.getProcessGroup();
        this.processorsMap = new HashMap<>(connectionOrderVisitor.getProcessorsMap());
        this.visitedProcessGroups = new HashMap<>(connectionOrderVisitor.getVisitedProcessGroups());
        this.visitedProcessors = new HashMap<>(connectionOrderVisitor.getVisitedProcessors());
        this.allConnections = new HashSet<>(connectionOrderVisitor.getAllConnections());
    }

    public String getProcessGroupId() {
        return processGroup.getDto().getId();
    }

    public NifiVisitableProcessGroup getProcessGroup() {
        return processGroup;
    }

    public Map<String, ProcessorDTO> getProcessorsMap() {
        return processorsMap;
    }

    public Map<String, NifiVisitableProcessor> getVisitedProcessors() {
        return visitedProcessors;
    }

    public Map<String, NifiVisitableProcessGroup> getVisitedProcessGroups() {
        return visitedProcessGroups;
    }

    public Set<NifiVisitableConnection> getAllConnections() {
        return allConnections;
    }
}
