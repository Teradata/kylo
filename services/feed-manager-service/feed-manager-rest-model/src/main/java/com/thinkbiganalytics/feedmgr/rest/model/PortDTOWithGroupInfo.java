package com.thinkbiganalytics.feedmgr.rest.model;
/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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
import com.wordnik.swagger.annotations.ApiModelProperty;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.PositionDTO;

import java.util.Collection;
import java.util.Set;

/**
 * Decorate the PortDTO and add the parent group names where the ports reside in.
 */
public class PortDTOWithGroupInfo extends PortDTO {

    private String destinationProcessGroupName;
    private String sourceProcessGroupName;

    private PortDTO portDTO;

    public PortDTOWithGroupInfo(PortDTO portDTO) {
        this.portDTO = portDTO;
    }

    public String getDestinationProcessGroupName() {
        return destinationProcessGroupName;
    }

    public void setDestinationProcessGroupName(String destinationProcessGroupName) {
        this.destinationProcessGroupName = destinationProcessGroupName;
    }

    public String getSourceProcessGroupName() {
        return sourceProcessGroupName;
    }

    public void setSourceProcessGroupName(String sourceProcessGroupName) {
        this.sourceProcessGroupName = sourceProcessGroupName;
    }


    @Override
    @ApiModelProperty("The name of the port.")
    public String getName() {
        return portDTO.getName();
    }

    @Override
    public void setName(String name) {
        portDTO.setName(name);
    }

    @Override
    @ApiModelProperty(
        value = "The state of the port.",
        allowableValues = "RUNNING, STOPPED, DISABLED"
    )
    public String getState() {
        return portDTO.getState();
    }

    @Override
    public void setState(String state) {
        portDTO.setState(state);
    }

    @Override
    @ApiModelProperty(
        value = "The type of port.",
        allowableValues = "INPUT_PORT, OUTPUT_PORT"
    )
    public String getType() {
        return portDTO.getType();
    }

    @Override
    public void setType(String type) {
        portDTO.setType(type);
    }

    @Override
    @ApiModelProperty("The number of tasks that should be concurrently scheduled for the port.")
    public Integer getConcurrentlySchedulableTaskCount() {
        return portDTO.getConcurrentlySchedulableTaskCount();
    }

    @Override
    public void setConcurrentlySchedulableTaskCount(Integer concurrentlySchedulableTaskCount) {
        portDTO.setConcurrentlySchedulableTaskCount(concurrentlySchedulableTaskCount);
    }

    @Override
    @ApiModelProperty("The comments for the port.")
    public String getComments() {
        return portDTO.getComments();
    }

    @Override
    public void setComments(String comments) {
        portDTO.setComments(comments);
    }

    @Override
    @ApiModelProperty("Whether the port has incoming or output connections to a remote NiFi. This is only applicable when the port is running in the root group.")
    public Boolean isTransmitting() {
        return portDTO.isTransmitting();
    }

    @Override
    public void setTransmitting(Boolean transmitting) {
        portDTO.setTransmitting(transmitting);
    }

    @Override
    @ApiModelProperty("The user groups that are allowed to access the port.")
    public Set<String> getGroupAccessControl() {
        return portDTO.getGroupAccessControl();
    }

    @Override
    public void setGroupAccessControl(Set<String> groupAccessControl) {
        portDTO.setGroupAccessControl(groupAccessControl);
    }

    @Override
    @ApiModelProperty("The users that are allowed to access the port.")
    public Set<String> getUserAccessControl() {
        return portDTO.getUserAccessControl();
    }

    @Override
    public void setUserAccessControl(Set<String> userAccessControl) {
        portDTO.setUserAccessControl(userAccessControl);
    }

    @Override
    @ApiModelProperty("Gets the validation errors from this port. These validation errors represent the problems with the port that must be resolved before it can be started.")
    public Collection<String> getValidationErrors() {
        return portDTO.getValidationErrors();
    }

    @Override
    public void setValidationErrors(Collection<String> validationErrors) {
        portDTO.setValidationErrors(validationErrors);
    }

    @Override
    @ApiModelProperty("The id of the component.")
    public String getId() {
        return portDTO.getId();
    }

    @Override
    public void setId(String id) {
        portDTO.setId(id);
    }

    @Override
    @ApiModelProperty("The id of parent process group of this component if applicable.")
    public String getParentGroupId() {
        return portDTO.getParentGroupId();
    }

    @Override
    public void setParentGroupId(String parentGroupId) {
        portDTO.setParentGroupId(parentGroupId);
    }

    @Override
    @ApiModelProperty("The position of this component in the UI if applicable.")
    public PositionDTO getPosition() {
        return portDTO.getPosition();
    }

    @Override
    public void setPosition(PositionDTO position) {
        portDTO.setPosition(position);
    }

    @Override
    public int hashCode() {
        return portDTO.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return portDTO.equals(obj);
    }

    @Override
    public String toString() {
        return portDTO.toString();
    }




}
