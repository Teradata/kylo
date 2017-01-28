package com.thinkbiganalytics.nifi.v0.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v0
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

import com.thinkbiganalytics.nifi.rest.client.NiFiReportingTaskRestClient;

import org.apache.nifi.web.api.dto.ComponentStateDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Reporting task interaction is not supported in NiFi 0.x
 * Please upgrade Nifi and use the v1 version of the {@link com.thinkbiganalytics.nifi.rest.client.NiFiRestClient}
 */
public class NiFiReportingTaskClientV0 implements NiFiReportingTaskRestClient {

    @Override
    public ControllerServiceDTO createReportingTaskControllerService(@Nonnull ControllerServiceDTO controllerService) {
        return null;
    }

    @Override
    public Set<ControllerServiceDTO> findAllReportingTaskControllerServices() {
        return Collections.emptySet();
    }

    @Override
    public Optional<ControllerServiceDTO> findFirstControllerServiceByType(String controllerServiceType) {
        return Optional.empty();
    }

    @Override
    public ReportingTaskDTO createReportingTask(@Nonnull ReportingTaskDTO reportingTask) {
        return null;
    }

    @Override
    public Set<ReportingTaskDTO> findAllReportingTasks() {
        return Collections.emptySet();
    }

    @Override
    public Optional<ReportingTaskDTO> findFirstByType(@Nonnull String type) {
        return null;
    }

    @Override
    public Optional<ReportingTaskDTO> findById(@Nonnull String id) {
        return Optional.empty();
    }

    @Override
    public Optional<ReportingTaskDTO> findByName(@Nonnull String name) {
        return Optional.empty();
    }

    @Override
    public ReportingTaskDTO update(ReportingTaskDTO reportingTaskDTO) {
        return null;
    }

    @Override
    public ReportingTaskDTO delete(String id) {
        return null;
    }

    @Override
    public Optional<ComponentStateDTO> getState(String id) {
        return Optional.empty();
    }

}
