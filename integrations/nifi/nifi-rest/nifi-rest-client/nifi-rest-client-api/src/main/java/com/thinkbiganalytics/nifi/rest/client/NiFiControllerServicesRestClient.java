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
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentsEntity;
import org.apache.nifi.web.api.entity.UpdateControllerServiceReferenceRequestEntity;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Controller Services REST endpoint as a Java class.
 */
public interface NiFiControllerServicesRestClient {

    /**
     * States of controller services.
     */
    enum State {
        /**
         * The controller service is usable
         */
        ENABLED,

        /**
         * The controller service is editable
         */
        DISABLED
    }

    /**
     * Deletes a controller service.
     *
     * @param id the controller service id
     * @return the controller service, if found
     */
    @Nonnull
    Optional<ControllerServiceDTO> delete(@Nonnull String id);

    /**
     * Disables and deletes the specified controller service, asynchronously.
     *
     * @param id the controller service id
     * @return the controller service, if found
     */
    @Nonnull
    Future<Optional<ControllerServiceDTO>> disableAndDeleteAsync(@Nonnull String id);

    /**
     * Creates a new controller service.
     *
     * @param controllerService the controller service configuration details
     * @return the controller service
     */
    @Nonnull
    ControllerServiceDTO create(@Nonnull ControllerServiceDTO controllerService);

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
     * Gets the NiFi REST client associated with this controller service client.
     *
     * @return the NiFi REST client
     */
    NiFiRestClient getClient();

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


    /**
     * Updates the service.
     * 1. this will disable the service
     * 2. stop all referencing components
     * 3. update the service
     * 4. enable the service
     * 5. restore components back to their previous state
     *
     * @param controllerService the service to update with the updated properties
     */
    ControllerServiceDTO updateServiceAndReferencingComponents(ControllerServiceDTO controllerService);

    /**
     * Updates the state of the specified controller service.
     *
     * @param id    the controller service id
     * @param state the new state
     * @return the controller service
     * @throws NifiClientRuntimeException     if the state cannot be changed
     * @throws NifiComponentNotFoundException if the controller service does not exist
     */
    ControllerServiceDTO updateStateById(@Nonnull String id, @Nonnull State state);


    /**
     * Returns the references of a controller service
     *
     * @param id the id of the service
     * @return the references on this service
     */
    Optional<ControllerServiceReferencingComponentsEntity> getReferences(@Nonnull String id);


    /**
     * Updates the Controller service references
     *
     * @param id     the id of the service
     * @param update the request for the references to update
     * @return the references that were updated
     */
    ControllerServiceReferencingComponentsEntity updateReferences(@Nonnull String id, @Nonnull UpdateControllerServiceReferenceRequestEntity update);
}
