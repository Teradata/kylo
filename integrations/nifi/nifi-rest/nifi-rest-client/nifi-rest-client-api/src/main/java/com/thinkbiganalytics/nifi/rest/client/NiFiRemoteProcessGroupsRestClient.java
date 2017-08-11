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

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Process Groups REST endpoint as a Java class.
 */
public interface NiFiRemoteProcessGroupsRestClient {

    /**
     * Gets a process group.
     *
     * @param processGroupId the process group id
     * @param recursive      {@code true} to include all encapsulated components, or {@code false} for just the immediate children
     * @param verbose        {@code true} to include any encapsulated components, or {@code false} for just details about the process group
     * @return the process group, if found
     */
    @Nonnull
    Optional<RemoteProcessGroupDTO> findById(@Nonnull String processGroupId);


    /**
     * Updates a process group.
     *
     * @param processGroup the process group
     * @return the updated process group
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    @Nonnull
    RemoteProcessGroupDTO update(@Nonnull RemoteProcessGroupDTO processGroup);
}
