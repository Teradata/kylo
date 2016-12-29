package com.thinkbiganalytics.feedmgr.rest.model;

import java.util.List;

/**
 * Holds a list of ReusableConnectionInfo passed in as a POST Created by sr186054 on 12/29/16.
 */
public class NiFiTemplateFlowRequest {

    private String templateId;
    /**
     * Info containing how this template should connect to the reusable flows
     */
    private List<ReusableTemplateConnectionInfo> connectionInfo;


    public NiFiTemplateFlowRequest() {

    }


    public List<ReusableTemplateConnectionInfo> getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(List<ReusableTemplateConnectionInfo> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}
