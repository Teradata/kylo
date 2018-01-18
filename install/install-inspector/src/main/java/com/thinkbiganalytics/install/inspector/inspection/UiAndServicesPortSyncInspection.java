package com.thinkbiganalytics.install.inspector.inspection;

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
