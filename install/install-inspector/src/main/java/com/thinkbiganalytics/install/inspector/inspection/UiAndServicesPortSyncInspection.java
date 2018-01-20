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


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UiAndServicesPortSyncInspection extends AbstractInspection<UiAndServicesPortSyncInspection.ServiceProperty, UiAndServicesPortSyncInspection.UiProperty> {

    class ServiceProperty {
        @Value("${server.port}")
        private String serverPort;
    }

    class UiProperty {
        @Value("${zuul.routes.api.url}")
        private String url;
    }

    @Override
    public String getName() {
        return "Kylo UI and Services Port Sync";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo UI can connect to to Kylo Services";
    }

    @Override
    public InspectionStatus inspect(ServiceProperty servicesProperties, UiProperty uiProperties) {
        boolean valid = servicesProperties.serverPort.equals(Integer.toString(URI.create(uiProperties.url).getPort()));
        InspectionStatus inspectionStatus = new InspectionStatus(valid);
        if (!valid) {
            inspectionStatus.setError("'server.port' property in kylo-services/conf/application.properties does not match port number specified by 'zuul.routes.api.url' property in kylo-ui/conf/application.properties");
        }
        return inspectionStatus;
    }

    @Override
    public ServiceProperty getServicesProperties() {
        return new ServiceProperty();
    }

    @Override
    public UiProperty getUiProperties() {
        return new UiProperty();
    }


}
