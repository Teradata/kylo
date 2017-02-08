package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;

import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Defines an interface for Ambari clients
 */
public interface AmbariClient {

    /**
     * @return a list of cluster names defined in Ambari
     */
    List<String> getAmbariClusterNames();

    /**
     * @return Summary for all services
     */
    ServiceComponentInfoSummary getServiceComponentInfo(List<String> clusterNames, String services)
        throws RestClientException;

    /**
     * @return Summary for all alerts
     */
    AlertSummary getAlerts(List<String> clusterNames, String services) throws RestClientException;
}
