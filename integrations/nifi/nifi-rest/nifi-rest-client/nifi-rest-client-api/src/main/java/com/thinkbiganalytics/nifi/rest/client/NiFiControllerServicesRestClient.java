package com.thinkbiganalytics.nifi.rest.client;

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
     */
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
     * Updates a controller service.
     *
     * @param controllerService the controller service
     * @return the controller service
     * @throws NifiComponentNotFoundException if the controller service does not exist
     */
    @Nonnull
    ControllerServiceDTO update(@Nonnull ControllerServiceDTO controllerService);
}
