package com.thinkbiganalytics.nifi.rest.client.layout;

/**
 * Created by sr186054 on 11/8/16.
 */
public class AlignComponentsConfig {

    private Double centerX = 0.0d;
    private Double centerY = 0.0d;

    private Integer portHeight = 55;
    private Integer portWidth = 240;

    private Integer processGroupHeight = 170;
    private Integer processGroupWidth = 380;

    private Integer processGroupPaddingLeftRight = 150;
    private Integer processGroupPaddingTopBottom = 200;

    private Integer groupPadding = 200;

    public AlignComponentsConfig() {

    }


    public Double getCenterX() {
        return centerX;
    }

    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    public Double getCenterY() {
        return centerY;
    }

    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    public Integer getPortHeight() {
        return portHeight;
    }

    public void setPortHeight(Integer portHeight) {
        this.portHeight = portHeight;
    }

    public Integer getPortWidth() {
        return portWidth;
    }

    public void setPortWidth(Integer portWidth) {
        this.portWidth = portWidth;
    }

    public Integer getProcessGroupHeight() {
        return processGroupHeight;
    }

    public void setProcessGroupHeight(Integer processGroupHeight) {
        this.processGroupHeight = processGroupHeight;
    }

    public Integer getProcessGroupWidth() {
        return processGroupWidth;
    }

    public void setProcessGroupWidth(Integer processGroupWidth) {
        this.processGroupWidth = processGroupWidth;
    }

    public Integer getProcessGroupPaddingLeftRight() {
        return processGroupPaddingLeftRight;
    }

    public void setProcessGroupPaddingLeftRight(Integer processGroupPaddingLeftRight) {
        this.processGroupPaddingLeftRight = processGroupPaddingLeftRight;
    }

    public Integer getProcessGroupPaddingTopBottom() {
        return processGroupPaddingTopBottom;
    }

    public void setProcessGroupPaddingTopBottom(Integer processGroupPaddingTopBottom) {
        this.processGroupPaddingTopBottom = processGroupPaddingTopBottom;
    }

    public Integer getGroupPadding() {
        return groupPadding;
    }

    public void setGroupPadding(Integer groupPadding) {
        this.groupPadding = groupPadding;
    }
}
