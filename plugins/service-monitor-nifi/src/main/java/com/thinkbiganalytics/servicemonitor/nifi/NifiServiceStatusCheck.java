package com.thinkbiganalytics.servicemonitor.nifi;

/*-
 * #%L
 * thinkbig-service-monitor-nifi
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


import com.thinkbiganalytics.nifi.provenance.NiFiProvenanceConstants;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * Check the status of NiFi and the required Kylo Reporting Task
 */
public class NifiServiceStatusCheck implements ServiceStatusCheck {

    @Autowired
    @Qualifier("nifiRestClient")
    private LegacyNifiRestClient nifiRestClient;

    public NifiServiceStatusCheck() {
    }


    @Override
    public ServiceStatusResponse healthCheck() {

        String serviceName = "NiFi";

        return new DefaultServiceStatusResponse(serviceName, Arrays.asList(nifiStatus(), nifiReportingTaskStatus()));
    }


    /**
     * Check to see if NiFi is running
     *
     * @return the status of NiFi
     */
    private ServiceComponent nifiStatus() {

        String componentName = "NiFi";
        ServiceComponent component = null;

        try {
            AboutDTO aboutEntity = nifiRestClient.getNifiVersion();

            String nifiVersion = aboutEntity.getVersion();
            component =
                new DefaultServiceComponent.Builder(componentName + " - " + nifiVersion, ServiceComponent.STATE.UP)
                    .message("NiFi is up.").build();
        } catch (Exception e) {
            component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).exception(e).build();
        }
        return component;
    }

    /**
     * Check to see if the Reporting task is configured and running
     *
     * @return the status of the reporting task
     */
    private ServiceComponent nifiReportingTaskStatus() {

        String componentName = "Kylo Reporting Task";
        ServiceComponent component = null;

        try {
            Optional<ReportingTaskDTO> task = nifiRestClient.getNiFiRestClient().reportingTasks().findFirstByType(NiFiProvenanceConstants.NiFiKyloProvenanceEventReportingTaskType);
            if (task.isPresent()) {
                if ("RUNNING".equalsIgnoreCase(task.get().getState())) {
                    component =
                        new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.UP)
                            .message("The reporting task is up and running").build();
                } else {
                    component =
                        new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN)
                            .addErrorAlert("Reporting Task Status", "The Reporting task is currently " + task.get().getState() + ".  It needs to be RUNNING to process jobs in Kylo", new Date())
                            .message("The reporting task is not running.  No jobs will be processed in Kylo until it is running").build();
                }
            } else {
                component =
                    new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN)
                        .addErrorAlert("Reporting Task Status",
                                       "The Reporting task does not exist. A " + NiFiProvenanceConstants.NiFiKyloProvenanceEventReportingTaskType + " needs to be RUNNING to process jobs in Kylo",
                                       new Date())
                        .message("The reporting task does not exist.  No jobs will be processed in Kylo until the Reporting task is configured and running").build();
            }


        } catch (Exception e) {
            component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).exception(e).build();
        }
        return component;
    }


    public void setNifiRestClient(LegacyNifiRestClient nifiRestClient) {
        this.nifiRestClient = nifiRestClient;
    }
}
