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

import org.apache.nifi.web.api.dto.ConnectionDTO;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Connections REST endpoint as a Java class.
 */
public interface NiFiConnectionsRestClient {

    /**
     * Deletes a connection.
     *
     * @param processGroupId the process group id
     * @param connectionId   the connection id
     * @return the connection, if found
     * @throws NifiClientRuntimeException if the operation times out
     */
    @Nonnull
    Optional<ConnectionDTO> delete(@Nonnull String processGroupId, @Nonnull String connectionId);

    /**
     * Drops the contents of the queue for the specified connection.
     *
     * @param processGroupId the process group id
     * @param connectionId   the connection id
     * @return {@code true} if the contents of the queue were deleted, or {@code false} if the process group or connection does not exist
     */
    boolean deleteQueue(@Nonnull String processGroupId, @Nonnull String connectionId);
}
