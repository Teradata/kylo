package com.thinkbiganalytics.install.inspector.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NifiConnectionCheck extends DisabledConfigCheck {

    private final Logger log = LoggerFactory.getLogger(NifiConnectionCheck.class);

    @Override
    public String getName() {
        return "Nifi Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo Services can connect to Nifi";
    }

}
