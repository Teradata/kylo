package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1.2
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import org.apache.nifi.web.api.dto.ControllerServiceApiDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Created by sr186054 on 6/29/17.
 */
public class NiFiControllerServicesRestClientV1_2  extends NiFiControllerServicesRestClientV1{

    public NiFiControllerServicesRestClientV1_2(NiFiRestClientV1 client) {
        super(client);
    }


    @Nonnull
    @Override
    public Set<DocumentedTypeDTO> getTypes(@Nonnull final String serviceType) {
        // Find the bundle that contains the API
        final Optional<ControllerServiceApiDTO> controllerServiceApi = getTypes().stream()
            .map(DocumentedTypeDTO::getControllerServiceApis)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(api -> api.getType().equals(serviceType))
            .findAny();

        // Query for types implementing the API
        final Map<String, Object> query = new HashMap<>();
        query.put("serviceType", serviceType);

        controllerServiceApi.map(ControllerServiceApiDTO::getBundle)
            .ifPresent(bundle -> {
                query.put("serviceBundleGroup", bundle.getGroup());
                query.put("serviceBundleArtifact", bundle.getArtifact());
                query.put("serviceBundleVersion", bundle.getVersion());
            });

        return Optional.ofNullable(client.get("/flow/controller-service-types", query, ControllerServiceTypesEntity.class))
            .map(ControllerServiceTypesEntity::getControllerServiceTypes)
            .orElseGet(Collections::emptySet);
    }
}
