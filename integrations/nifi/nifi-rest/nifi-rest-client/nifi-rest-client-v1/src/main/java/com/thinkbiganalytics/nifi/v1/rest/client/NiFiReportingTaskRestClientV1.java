package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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

import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiReportingTaskRestClient;

import org.apache.nifi.web.api.dto.ComponentStateDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ReportingTaskEntity;
import org.apache.nifi.web.api.entity.ReportingTasksEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Implements a {@link NiFiConnectionsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiReportingTaskRestClientV1 implements NiFiReportingTaskRestClient {


    /**
     * path to create a controller service
     **/
    private static final String CREATE_CONTROLLER_SERVICE_PATH = "/controller/controller-services";

    /**
     * path to create a reporting task
     **/
    private static final String CREATE_REPORTING_TASK_PATH = "/controller/reporting-tasks";

    /**
     * Path to list all reporting tasks
     */
    private static final String LIST_REPORTING_TASK_PATH = "/flow/reporting-tasks";

    /**
     * Path to reporting task entity
     */
    private static final String REPORTING_TASK_PATH = "/reporting-tasks/";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiConnectionsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiReportingTaskRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Override
    public ControllerServiceDTO createReportingTaskControllerService(@Nonnull ControllerServiceDTO controllerService) {
        ControllerServiceEntity entity = new ControllerServiceEntity();
        entity.setComponent(controllerService);
        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        ControllerServiceEntity updatedService = client.post(CREATE_CONTROLLER_SERVICE_PATH, entity, ControllerServiceEntity.class);
        if (updatedService != null) {
            return updatedService.getComponent();
        } else {
            return null;
        }

    }

    /**
     * Find all controller services used for reporting tasks
     *
     * @return the controller services
     */
    public Set<ControllerServiceDTO> findAllReportingTaskControllerServices() {
        Set<ControllerServiceEntity> controllerServiceEntities = client.get("/flow/controller/controller-services", null, ControllerServicesEntity.class).getControllerServices();
        if (controllerServiceEntities != null && !controllerServiceEntities.isEmpty()) {
            return controllerServiceEntities.stream().map(controllerService -> controllerService.getComponent())
                .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Find the first controller service matching the type
     *
     * @param controllerServiceType the type of controller service
     * @return the controller service
     */
    @Override
    public Optional<ControllerServiceDTO> findFirstControllerServiceByType(String controllerServiceType) {

        return Optional
            .ofNullable(findAllReportingTaskControllerServices().stream().filter(controllerService -> controllerService.getType().equalsIgnoreCase(controllerServiceType)).findFirst().orElse(null));

    }


    /**
     * create a new reporting task
     *
     * @param reportingTask the reporting task
     * @return the reporting task
     */
    @Override
    public ReportingTaskDTO createReportingTask(@Nonnull ReportingTaskDTO reportingTask) {

        ReportingTaskEntity updatedReportingTask = client.post(CREATE_REPORTING_TASK_PATH, toReportingTaskEntity(reportingTask, 0L), ReportingTaskEntity.class);
        if (updatedReportingTask != null) {
            return updatedReportingTask.getComponent();
        } else {
            return null;
        }
    }


    /**
     * finds all the reporting tasks configured
     *
     * @return all the reporting tasks configured
     */
    @Override
    public Set<ReportingTaskDTO> findAllReportingTasks() {
        Set<ReportingTaskEntity> reportingTaskEntities = client.get(LIST_REPORTING_TASK_PATH, null, ReportingTasksEntity.class).getReportingTasks();
        if (reportingTaskEntities != null && !reportingTaskEntities.isEmpty()) {
            return reportingTaskEntities.stream().map(t -> t.getComponent())
                .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Finds the first reporting task matching the supplied type
     *
     * @param type the type of the reporting task
     * @return the reporting task
     */
    @Override
    public Optional<ReportingTaskDTO> findFirstByType(@Nonnull String type) {
        return Optional.ofNullable(findAllReportingTasks().stream().filter(task -> task.getType().equalsIgnoreCase(type)).findFirst().orElse(null));
    }

    /**
     * Find the reporting task matching the id
     *
     * @param id the id of the reporting task
     * @return the reporting task
     */
    @Override
    public Optional<ReportingTaskDTO> findById(@Nonnull String id) {
        return Optional.ofNullable(client.get(REPORTING_TASK_PATH + id, null, ReportingTaskEntity.class)).map(task -> task.getComponent());

    }

    /**
     * Find the reporting task entity matching the id
     *
     * @param id the id of the reporting task
     * @return the reporting task
     */
    private Optional<ReportingTaskEntity> findReportingTaskEntityById(@Nonnull String id) {
        return Optional.ofNullable(client.get(REPORTING_TASK_PATH + id, null, ReportingTaskEntity.class));

    }


    /**
     * Find the first reporting task matching the name (ignore case)
     *
     * @param name the name of the reporting task
     * @return the reporting task
     */
    @Override
    public Optional<ReportingTaskDTO> findByName(@Nonnull String name) {
        return Optional.ofNullable(findAllReportingTasks().stream().filter(task -> task.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    /**
     * update a reporting task
     *
     * @param reportingTaskDTO the reporting task
     * @return the updated reporting task, null if there was an error
     */
    @Override
    public ReportingTaskDTO update(final ReportingTaskDTO reportingTaskDTO) {
        String id = reportingTaskDTO.getId();

        Optional<ReportingTaskDTO> updated = findReportingTaskEntityById(id)
            .flatMap(current -> {
                         try {
                             ReportingTaskEntity entity = client.put(REPORTING_TASK_PATH + id, toReportingTaskEntity(reportingTaskDTO, current.getRevision().getVersion()), ReportingTaskEntity.class);

                             if (entity != null) {
                                 return Optional.of(entity.getComponent());
                             } else {
                                 return null;
                             }
                         } catch (Exception e) {
                             return Optional.empty();
                         }
                     }
            );
        if (updated.isPresent()) {
            return updated.get();
        } else {
            return null;
        }

    }

    /**
     * Delete a reporting task
     *
     * @param id the id of the reporting task to delete
     * @return the deleted reporting task
     */
    @Override
    public ReportingTaskDTO delete(String id) {
        ReportingTaskEntity entity = client.delete(REPORTING_TASK_PATH + id, null, ReportingTaskEntity.class);
        if (entity != null) {
            return entity.getComponent();
        } else {
            return null;
        }
    }

    /**
     * Return the state of a reporting task
     *
     * @param id the id of the reporting task
     * @return the state of the reporting task
     */
    @Override
    public Optional<ComponentStateDTO> getState(String id) {
        return Optional.ofNullable(client.get(REPORTING_TASK_PATH + id + "/state", null, ComponentStateDTO.class));
    }


    private ReportingTaskEntity toReportingTaskEntity(ReportingTaskDTO reportingTask, Long versionId) {
        ReportingTaskEntity entity = new ReportingTaskEntity();
        entity.setComponent(reportingTask);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(versionId);
        entity.setRevision(revision);
        return entity;
    }
}
