package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class to hold ProcessGroups that need to be Rendered Created by sr186054 on 11/9/16.
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