package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UiAndServicesPortSyncInspection extends InspectionBase {

    private static final String SERVER_PORT = "server.port";
    private static final String ZUUL_ROUTES_API_URL = "zuul.routes.api.url";

    public UiAndServicesPortSyncInspection() {
        setDocsUrl("/installation/KyloApplicationProperties.html#common-configuration-properties");
        setName("Kylo UI and Services Port Sync");
        setDescription("Checks whether Kylo UI can connect to to Kylo Services");
    }

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        String servicesProperty = configuration.getServicesProperty(SERVER_PORT);
        String uiProperty = configuration.getUiProperty(ZUUL_ROUTES_API_URL);
        boolean valid = servicesProperty.equals(Integer.toString(URI.create(uiProperty).getPort()));
        InspectionStatus inspectionStatus = new InspectionStatus(valid);
        if (!valid) {
            inspectionStatus.addError("'server.port' property in kylo-services/conf/application.properties does not match port number specified by '" + ZUUL_ROUTES_API_URL
                                      + "' property in kylo-ui/conf/application.properties");
        }
        return inspectionStatus;
    }
}
