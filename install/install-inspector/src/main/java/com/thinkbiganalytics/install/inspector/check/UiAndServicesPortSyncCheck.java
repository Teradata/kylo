package com.thinkbiganalytics.install.inspector.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UiAndServicesPortSyncCheck extends AbstractConfigCheck {

    private final Logger log = LoggerFactory.getLogger(UiAndServicesPortSyncCheck.class);

    @Override
    public String getName() {
        return "Kylo UI and Services Port Sync";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo UI can connect to to Kylo Services";
    }
}
