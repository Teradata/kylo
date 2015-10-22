/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jira.servicestatus;

import com.thinkbiganalytics.jira.JiraClient;
import com.thinkbiganalytics.jira.domain.ServerInfo;
import com.thinkbiganalytics.pipelinecontroller.servicestatus.ServiceComponent;
import com.thinkbiganalytics.pipelinecontroller.servicestatus.ServiceStatusCheck;
import com.thinkbiganalytics.pipelinecontroller.servicestatus.ServiceStatusResponse;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by sr186054 on 10/21/15.
 */
public class JiraServiceStatusCheck implements ServiceStatusCheck {


    @Inject
    public JiraClient jiraClient;

    @Override
    public ServiceStatusResponse healthCheck() {
        ServiceStatusResponse response = null;
        //Only perform the check if the user specified the JIRA config properties
        if(jiraClient.isHostConfigured()) {
            String serviceName = "JIRA";
            ServiceComponent component = null;
            try {
                ServerInfo info = jiraClient.getServerInfo();

                component = new ServiceComponent.Builder("JIRA v." + info.getVersion(), ServiceComponent.STATE.UP).message("JIRA is up").build();
            } catch (Exception e) {
                component = new ServiceComponent.Builder(serviceName, ServiceComponent.STATE.DOWN).exception(e).build();
            }
            response = new ServiceStatusResponse(serviceName, Arrays.asList(component));
        }
        return response;

    }

    public void setJiraClient(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }
}
