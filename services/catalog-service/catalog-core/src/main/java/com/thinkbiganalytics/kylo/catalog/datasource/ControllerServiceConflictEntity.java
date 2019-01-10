package com.thinkbiganalytics.kylo.catalog.datasource;
/*-
 * #%L
 * kylo-catalog-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import java.util.List;
import java.util.Map;

public class ControllerServiceConflictEntity {

    String type;
    String name;
    Map<String,String> identityProperties;
    List<ControllerServiceDTO> matchingServices;

    public ControllerServiceConflictEntity(String name, Map<String, String> identityProperties, List<ControllerServiceDTO> matchingServices) {
        this.type = ControllerServiceConflictEntity.class.getSimpleName();
        this.name = name;
        this.identityProperties = identityProperties;
        this.matchingServices = matchingServices;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getIdentityProperties() {
        return identityProperties;
    }

    public List<ControllerServiceDTO> getMatchingServices() {
        return matchingServices;
    }

    public String getType() {
        return type;
    }
}
