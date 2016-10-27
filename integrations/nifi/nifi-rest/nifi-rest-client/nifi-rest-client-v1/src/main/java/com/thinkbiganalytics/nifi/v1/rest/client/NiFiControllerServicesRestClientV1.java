package com.thinkbiganalytics.nifi.v1.rest.client;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiControllerServicesRestClient} for communicating with NiFi v1.0.
 */
public class NiFiControllerServicesRestClientV1 implements NiFiControllerServicesRestClient {

    /** Base path for controller service requests */
    private static final String BASE_PATH = "/controller-services/";

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiControllerServicesRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiControllerServicesRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ControllerServiceDTO> delete(@Nonnull final String id) {
        return findEntityById(id)
                .flatMap(controllerService -> {
                    final Long version = controllerService.getRevision().getVersion();
                    try {
                        return Optional.of(client.delete(BASE_PATH + id, ImmutableMap.of("version", version), ControllerServiceEntity.class).getComponent());
                    } catch (final NotFoundException e) {
                        return Optional.empty();
                    }
                });
    }

    @Nonnull
    @Override
    public Set<ControllerServiceDTO> findAll() {
        return client.get("/flow/process-groups/root/controller-services", null, ControllerServicesEntity.class)
                .getControllerServices().stream()
                .map(ControllerServiceEntity::getComponent)
                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Optional<ControllerServiceDTO> findById(@Nonnull final String id) {
        return findEntityById(id).map(ControllerServiceEntity::getComponent);
    }

    @Nonnull
    @Override
    public Set<DocumentedTypeDTO> getTypes() {
        return client.get("/flow/controller-service-types", null, ControllerServiceTypesEntity.class).getControllerServiceTypes();
    }

    @Nonnull
    @Override
    public ControllerServiceDTO update(@Nonnull final ControllerServiceDTO controllerService) {
        return findEntityById(controllerService.getId())
                .flatMap(current -> {
                    final ControllerServiceEntity entity = new ControllerServiceEntity();
                    entity.setComponent(controllerService);

                    final RevisionDTO revision = new RevisionDTO();
                    revision.setVersion(current.getRevision().getVersion());
                    entity.setRevision(revision);

                    try {
                        return Optional.of(client.put(BASE_PATH + controllerService.getId(), entity, ControllerServiceEntity.class).getComponent());
                    } catch (final NotFoundException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new NifiComponentNotFoundException(controllerService.getId(), NifiConstants.NIFI_COMPONENT_TYPE.CONTROLLER_SERVICE, null));
    }

    /**
     * Gets a controller service entity.
     *
     * @param id the controller service id
     * @return the controller service entity, if found
     */
    @Nonnull
    private Optional<ControllerServiceEntity> findEntityById(@Nonnull final String id) {
        try {
            return Optional.of(client.get(BASE_PATH + id, null, ControllerServiceEntity.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }
}
