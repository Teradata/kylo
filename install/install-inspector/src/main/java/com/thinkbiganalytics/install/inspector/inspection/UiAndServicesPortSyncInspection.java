package com.thinkbiganalytics.install.inspector.inspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiAndServicesPortSyncInspection extends AbstractInspection {

    private final Logger log = LoggerFactory.getLogger(UiAndServicesPortSyncInspection.class);

    @Override
    public String getName() {
        return "Kylo UI and Services Port Sync";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo UI can connect to to Kylo Services";
    }

    @Override
    public InspectionStatus inspect(Object servicesProperties, Object uiProperties) {
        return InspectionStatus.INVALID;
    }
}
