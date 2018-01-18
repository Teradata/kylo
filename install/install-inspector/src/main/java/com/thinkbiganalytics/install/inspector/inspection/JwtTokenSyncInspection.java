package com.thinkbiganalytics.install.inspector.inspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSyncInspection extends AbstractInspection<JwtTokenSyncInspection.JwtProperties, JwtTokenSyncInspection.JwtProperties> {

    private final Logger log = LoggerFactory.getLogger(JwtTokenSyncInspection.class);

    class JwtProperties {
        @Value("${security.jwt.key}")
        private String jwtKey;
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
    public InspectionStatus inspect(JwtProperties servicesProperties, JwtProperties uiProperties) {
        return new InspectionStatus(servicesProperties.jwtKey.equals(uiProperties.jwtKey));
    }

    @Override
    public JwtProperties getServicesProperties() {
        return new JwtProperties();
    }

    @Override
    public JwtProperties getUiProperties() {
        return new JwtProperties();
    }
}
