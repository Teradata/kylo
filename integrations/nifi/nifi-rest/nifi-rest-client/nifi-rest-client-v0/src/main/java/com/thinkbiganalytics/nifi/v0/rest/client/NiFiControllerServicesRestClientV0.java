package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiControllerServicesRestClient} for communicating with NiFi v0.6.
 */
public class NiFiControllerServicesRestClientV0 implements NiFiControllerServicesRestClient {

    /** Base path for controller service requests */
    private static final String BASE_PATH = "/controller/controller-services";

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiControllerServicesRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiControllerServicesRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ControllerServiceDTO> delete(@Nonnull final String id) {
        try {
            return Optional.of(client.delete(BASE_PATH + "/" + client.getClusterType() + "/" + id, new HashMap<>(), ControllerServiceEntity.class).getControllerService());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Set<ControllerServiceDTO> findAll() {
        return client.get(BASE_PATH + "/" + client.getClusterType(), null, ControllerServicesEntity.class).getControllerServices();
    }

    @Nonnull
    @Override
    public Optional<ControllerServiceDTO> findById(@Nonnull final String id) {
        try {
            return Optional.of(client.get(BASE_PATH + "/" + client.getClusterType() + "/" + id, null, ControllerServiceEntity.class).getControllerService());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Set<DocumentedTypeDTO> getTypes() {
        return client.get("/controller/controller-service-types", null, ControllerServiceTypesEntity.class).getControllerServiceTypes();
    }

    @Nonnull
    @Override
    public ControllerServiceDTO update(@Nonnull final ControllerServiceDTO controllerService) {
        final ControllerServiceEntity entity = new ControllerServiceEntity();
        entity.setControllerService(controllerService);

        try {
            return client.put(BASE_PATH + "/" + client.getClusterType() + "/" + controllerService.getId(), entity, ControllerServiceEntity.class).getControllerService();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(controllerService.getId(), NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, e);
        }
    }
}
