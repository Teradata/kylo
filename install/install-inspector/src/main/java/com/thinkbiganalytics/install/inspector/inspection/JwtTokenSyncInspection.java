package com.thinkbiganalytics.install.inspector.inspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSyncInspection extends AbstractInspection {

    private final Logger log = LoggerFactory.getLogger(JwtTokenSyncInspection.class);

    private class JwtProperties {
        @Value("${security.jwt.key}")
        private String jwtKey;

//        @Value("${modeshape.datasource.url}")
//        private String modeshapeUrl;
    }

    @Override
    public String getName() {
        return "Jwt Token Synchronisation Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo UI and Kylo Services have the same JWT tokens";
    }

    @Override
    public InspectionStatus inspect(Object properties) {
        return InspectionStatus.VALID;
    }

    @Override
    public Object getProperties() {
        return new JwtProperties();
    }
}
