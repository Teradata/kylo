package com.thinkbiganalytics.nifi.rest.model;
/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.List;

/**
 * Created by sr186054 on 12/7/17.
 */
public class VersionedProcessGroup {

    ProcessGroupDTO processGroupPriorToVersioning;

    List<ProcessorDTO> inputProcessorsPriorToDisabling;

    ProcessGroupDTO versionedProcessGroup;

    String processGroupName;

    String versionedProcessGroupName;
    List<ConnectionDTO> deletedInputPortConnections;

    public VersionedProcessGroup() {
    }

    public List<ProcessorDTO> getInputProcessorsPriorToDisabling() {
        return inputProcessorsPriorToDisabling;
    }

    public void setInputProcessorsPriorToDisabling(List<ProcessorDTO> inputProcessorsPriorToDisabling) {
        this.inputProcessorsPriorToDisabling = inputProcessorsPriorToDisabling;
    }

    public ProcessGroupDTO getProcessGroupPriorToVersioning() {
        return processGroupPriorToVersioning;
    }

    public void setProcessGroupPriorToVersioning(ProcessGroupDTO processGroupPriorToVersioning) {
        this.processGroupPriorToVersioning = processGroupPriorToVersioning;
    }

    public ProcessGroupDTO getVersionedProcessGroup() {
        return versionedProcessGroup;
    }

    public void setVersionedProcessGroup(ProcessGroupDTO versionedProcessGroup) {
        this.versionedProcessGroup = versionedProcessGroup;
    }

    public String getProcessGroupName() {
        return processGroupName;
    }

    public void setProcessGroupName(String processGroupName) {
        this.processGroupName = processGroupName;
    }

    public String getVersionedProcessGroupName() {
        return versionedProcessGroupName;
    }

    public void setVersionedProcessGroupName(String versionedProcessGroupName) {
        this.versionedProcessGroupName = versionedProcessGroupName;
    }

    public List<ConnectionDTO> getDeletedInputPortConnections() {
        return deletedInputPortConnections;
    }

    public void setDeletedInputPortConnections(List<ConnectionDTO> deletedInputPortConnections) {
        this.deletedInputPortConnections = deletedInputPortConnections;
    }
}
