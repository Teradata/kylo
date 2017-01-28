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

import org.apache.nifi.web.api.dto.ComponentStateDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Exposes NiFi REST endpoints for creating and managing Reporting tasks
 */
public interface NiFiReportingTaskRestClient {

    /**
     * Creates a controller services for use in a reporting task
     *
     * @param controllerService the controller service
     * @return the controller service
     */
    ControllerServiceDTO createReportingTaskControllerService(@Nonnull final ControllerServiceDTO controllerService);


    /**
     * Return all Controller services configured to work with reporting tasks
     *
     * @return the controller services
     */
    Set<ControllerServiceDTO> findAllReportingTaskControllerServices();


    /**
     * Find the first controller services for reporting tasks that match a given type
     *
     * @param controllerServiceType the type of controller service
     * @return the controller service
     */
    Optional<ControllerServiceDTO> findFirstControllerServiceByType(String controllerServiceType);

    /**
     * Creates a reporting task
     *
     * @param reportingTask the reporting task
     * @return the reporting task
     */
    ReportingTaskDTO createReportingTask(@Nonnull final ReportingTaskDTO reportingTask);


    /**
     * List all the reporting tasks configured
     */
    Set<ReportingTaskDTO> findAllReportingTasks();

    /**
     * find a Reporting Task by its type
     *
     * @param type the type of the reporting task
     * @return the reporting task
     */
    Optional<ReportingTaskDTO> findFirstByType(@Nonnull final String type);

    /**
     * Find a Reporting Task by its id
     *
     * @param id the id of the reporting task
     * @return the reporting task
     */
    Optional<ReportingTaskDTO> findById(@Nonnull final String id);

    /**
     * find a Reporting Task by its name
     *
     * @param name the name of the reporting task
     * @return the reporting task
     */
    Optional<ReportingTaskDTO> findByName(@Nonnull final String name);

    /**
     * update an existing reporting task
     *
     * @param reportingTaskDTO the reporting task
     * @return the updated reporting task
     */
    ReportingTaskDTO update(ReportingTaskDTO reportingTaskDTO);

    /**
     * delete a reporting task
     *
     * @param id the id of the reporting task to delete
     * @return the deleted reporting task
     */
    ReportingTaskDTO delete(String id);

    /**
     * Return the state of a given reporting task
     *
     * @param id the id of the reporting task
     * @return the state of the reporting task
     */
    Optional<ComponentStateDTO> getState(String id);


}
