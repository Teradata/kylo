package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * | Licensed under the Apache License, Version 2.0 (the "License");
 * | you may not use this file except in compliance with the License.
 * | You may obtain a copy of the License at
 * |
 * |     http://www.apache.org/licenses/LICENSE-2.0
 * |
 * | Unless required by applicable law or agreed to in writing, software
 * | distributed under the License is distributed on an "AS IS" BASIS,
 * | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * | See the License for the specific language governing permissions and
 * | limitations under the License.
 * #L%
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSyncInspection extends AbstractInspection<JwtTokenSyncInspection.JwtProperties, JwtTokenSyncInspection.JwtProperties> {

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
        boolean valid = servicesProperties.jwtKey.equals(uiProperties.jwtKey);
        InspectionStatus inspectionStatus = new InspectionStatus(valid);
        if (!valid) {
            inspectionStatus.setError("'security.jwt.key' property in kylo-services/conf/application.properties does not match 'security.jwt.key' property in kylo-ui/conf/application.properties");
        }
        return inspectionStatus;
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
