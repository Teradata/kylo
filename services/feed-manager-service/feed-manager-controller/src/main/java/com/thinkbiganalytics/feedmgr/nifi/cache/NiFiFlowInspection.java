package com.thinkbiganalytics.feedmgr.nifi.cache;

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

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NiFi process group inspection results
 */
public class NiFiFlowInspection {

    private int level;

    private String threadName;

    private String processGroupId;

    private String processGroupName;

    private Set<String> groupsToInspect = new HashSet<>();

    private ProcessGroupFlowDTO processGroupFlow;

    private NiFiFlowInspection parent;

    private List<NiFiFlowInspection> children = new ArrayList<>();

    private Long time = 0L;

    private boolean complete = false;


    public NiFiFlowInspection(String processGroupId, int level, NiFiFlowInspection parent, String threadName) {
        this.processGroupId = processGroupId;
        this.level = level;
        this.threadName = threadName;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }


    public String getThreadName() {
        return threadName;
    }


    public Set<String> getGroupsToInspect() {
        return groupsToInspect;
    }

    public void addGroupToInspect(String groupId) {
        groupsToInspect.add(groupId);
    }


    public void setGroupsToInspect(Set<String> groupsToInspect) {
        this.groupsToInspect = groupsToInspect;
    }

    public boolean needsFurtherInspection() {
        return groupsToInspect.size() > 0;
    }

    public ProcessGroupFlowDTO getProcessGroupFlow() {
        return processGroupFlow;
    }

    public void setProcessGroupFlow(ProcessGroupFlowDTO processGroupFlow) {
        this.processGroupFlow = processGroupFlow;
    }

    public String getProcessGroupId() {
        return processGroupId;
    }

    public void setProcessGroupName(String processGroupName) {
        this.processGroupName = processGroupName;
    }

    public boolean isRoot() {
        return processGroupFlow.getParentGroupId() == null;
    }

    public int getLevel() {
        return level;
    }

    public NiFiFlowInspection getParent() {
        return parent;
    }

    public boolean hasParent(String processGroupId) {
        if (parent != null && parent.getProcessGroupId().equalsIgnoreCase(processGroupId)) {
            return true;
        } else if (parent != null) {
            return parent.hasParent(processGroupId);
        }
        return false;
    }

    public String getProcessGroupName() {
        return processGroupName;
    }

    public void addChild(NiFiFlowInspection child) {
        children.add(child);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<NiFiFlowInspection> getChildren() {
        return children;
    }

    public List<NiFiFlowInspection> getThisAndAllChildren() {
        List<NiFiFlowInspection> allChildren = new ArrayList<>();
        allChildren.add(this);
        children.stream().forEach(c -> {
                                      allChildren.addAll(c.getThisAndAllChildren());
                                  }
        );
        return allChildren;
    }

    public List<ProcessorDTO> getAllProcessors() {
        List<NiFiFlowInspection> allChildren = getThisAndAllChildren();

        List<ProcessorDTO> processors = allChildren.stream()
            .flatMap(c -> c.getProcessGroupFlow().getFlow().getProcessors().stream())
            .map(e -> e.getComponent())
            .collect(Collectors.toList());
        //processors.addAll(getProcessGroupFlow().getFlow().getProcessors().stream().map(c -> c.getComponent()).collect(Collectors.toSet()));
        return processors;

    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
        setComplete(true);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
