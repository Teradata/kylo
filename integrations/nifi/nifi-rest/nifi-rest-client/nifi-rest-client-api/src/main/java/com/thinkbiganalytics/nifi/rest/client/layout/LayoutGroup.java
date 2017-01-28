package com.thinkbiganalytics.nifi.rest.client.layout;

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

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class to hold ProcessGroups that need to be Rendered
 */
public abstract class LayoutGroup {

    Set<ProcessGroupDTO> processGroupDTOs = new HashSet<>();

    private Integer height;


    private Integer groupNumber;

    private Double topY;

    private Double bottomY;


    public LayoutGroup() {

    }

    /**
     * Get the height of this grouping
     */
    public abstract Integer calculateHeight();

    public Integer getHeight() {
        if (height == null) {
            height = calculateHeight();
        }
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }


    public void setTopAndBottom(Double topY, Double bottomY) {
        this.topY = topY;
        this.bottomY = bottomY;
    }

    public void add(ProcessGroupDTO group) {
        processGroupDTOs.add(group);
    }

    public Double getMiddleY() {
        return getTopY() + getHeight() / 2;
    }

    public Double getMiddleY(Integer offset) {
        return getTopY() + (getHeight() / 2) - offset;
    }


    public Set<ProcessGroupDTO> getProcessGroupDTOs() {
        return processGroupDTOs;
    }

    public void setProcessGroupDTOs(Set<ProcessGroupDTO> processGroupDTOs) {
        this.processGroupDTOs = processGroupDTOs;
    }


    public Integer getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Integer groupNumber) {
        this.groupNumber = groupNumber;
    }


    public Double getTopY() {
        return topY;
    }

    public Double getBottomY() {
        return bottomY;
    }
}
