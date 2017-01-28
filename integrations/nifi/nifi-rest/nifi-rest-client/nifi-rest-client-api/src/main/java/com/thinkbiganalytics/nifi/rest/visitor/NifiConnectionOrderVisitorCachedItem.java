package com.thinkbiganalytics.nifi.rest.visitor;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableConnection;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;

import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A item that holds references to the NiFi processors and processgroups after it has been visited from the {@link com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowVisitor}
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
