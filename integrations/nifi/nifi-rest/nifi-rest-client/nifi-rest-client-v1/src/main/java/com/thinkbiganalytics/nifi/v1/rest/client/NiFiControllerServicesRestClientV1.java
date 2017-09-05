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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DocumentedTypeDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceTypesEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.UpdateControllerServiceReferenceRequestEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiControllerServicesRestClient} for communicating with NiFi v1.0.
 */
public class NiFiControllerServicesRestClientV1 extends AbstractNiFiControllerServicesRestClient {

    /**
     * Base path for controller service requests
     */
    private static final String BASE_PATH = "/controller-services/";

    /**
     * REST client for communicating with NiFi
     */
    protected final NiFiRestClientV1 client;

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

        Optional<ControllerServiceEntity> controllerServiceEntity = null;
        try {
            controllerServiceEntity = findEntityById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
        if (controllerServiceEntity.isPresent()) {
            return controllerServiceEntity
                .flatMap(controllerService -> {
                    final Long version = controllerService.getRevision().getVersion();
                    try {
                        return Optional.of(client.delete(BASE_PATH + id, ImmutableMap.of("version", version), ControllerServiceEntity.class).getComponent());
                    } catch (final NotFoundException e) {
                        return Optional.empty();
                    }
                });
        } else {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public ControllerServiceDTO create(@Nonnull ControllerServiceDTO controllerService) {
        final ControllerServiceEntity entity = new ControllerServiceEntity();
        entity.setComponent(controllerService);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        return client.post("/process-groups/root/controller-services", entity, ControllerServiceEntity.class).getComponent();
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

    @Override
    public NiFiRestClient getClient() {
        return client;
    }

    @Nonnull
    @Override
    public Set<DocumentedTypeDTO> getTypes() {
        return client.get("/flow/controller-service-types", null, ControllerServiceTypesEntity.class).getControllerServiceTypes();
    }

    @Nonnull
    @Override
    public Set<DocumentedTypeDTO> getTypes(@Nonnull final String serviceType) {
        return client.get("/flow/controller-service-types", Collections.singletonMap("serviceType", serviceType), ControllerServiceTypesEntity.class).getControllerServiceTypes();
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
                } catch (final ClientErrorException e) {
                    throw new NifiClientRuntimeException("Error updating controller service: " + e.getResponse().readEntity(String.class), e);
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
            return Optional.ofNullable(client.getWithoutErrorLogging(BASE_PATH + id, null, ControllerServiceEntity.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ControllerServiceReferencingComponentsEntity> getReferences(@Nonnull String id) {
        try {
        return Optional.ofNullable(client.get(BASE_PATH+id+"/references",null, ControllerServiceReferencingComponentsEntity.class));
    } catch (final NotFoundException e) {
        return Optional.empty();
    }
    }

    @Override
    public ControllerServiceReferencingComponentsEntity updateReferences(@Nonnull String id,@Nonnull UpdateControllerServiceReferenceRequestEntity update) {
        update.getId();
        return client.put(BASE_PATH+id+"/references",update,ControllerServiceReferencingComponentsEntity.class);
    }
}
