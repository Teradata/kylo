package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Controller Services REST endpoint as a Java class.
 */
public interface NiFiControllerServicesRestClient {

    /**
     * Deletes a controller service.
     *
     * @param id the controller service id
     * @return the controller service, if found
     */
    @Nonnull
    Optional<ControllerServiceDTO> delete(@Nonnull String id);

    /**
     * Gets all controller services.
     *
     * @return the controller services
     * @deprecated Find controller services by process group using {@link NiFiProcessGroupsRestClient#getControllerServices(String)}.
     */
    @Deprecated
    @Nonnull
    Set<ControllerServiceDTO> findAll();

    /**
     * Gets a controller service.
     *
     * @param id the controller service id
     * @return the controller service, if found
     */
    @Nonnull
    Optional<ControllerServiceDTO> findById(@Nonnull String id);

    /**
     * Retrieves the types of controller service that NiFi supports.
     *
     * @return the controller service types
     */
    @Nonnull
    Set<DocumentedTypeDTO> getTypes();

    /**
     * Retrieves the types of controller services that implement the specified type.
     *
     * @param serviceType the base type to find
     * @return the controller service types
     */
    @Nonnull
    Set<DocumentedTypeDTO> getTypes(@Nonnull String serviceType);

    /**
     * Updates a controller service.
     *
     * @param controllerService the controller service
     * @return the controller service
     * @throws NifiComponentNotFoundException if the controller service does not exist
     */
    @Nonnull
    ControllerServiceDTO update(@Nonnull ControllerServiceDTO controllerService);
}
