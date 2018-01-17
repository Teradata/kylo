package com.thinkbiganalytics.install.inspector.inspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSyncInspection extends AbstractInspection {

    private final Logger log = LoggerFactory.getLogger(JwtTokenSyncInspection.class);

    @Override
    public String getName() {
        return "Jwt Token Synchronisation Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo UI and Kylo Services have the same JWT tokens";
    }

}
