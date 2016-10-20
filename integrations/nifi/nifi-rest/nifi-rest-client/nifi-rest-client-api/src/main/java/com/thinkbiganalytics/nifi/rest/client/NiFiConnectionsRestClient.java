package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ListingRequestDTO;

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
     * @param connectionId the connection id
     * @return the connection, if found
     * @throws NifiClientRuntimeException if the operation times out
     */
    @Nonnull
    Optional<ConnectionDTO> delete(@Nonnull String processGroupId, @Nonnull String connectionId);

    /**
     * Drops the contents of the queue for the specified connection.
     * @param processGroupId the process group id
     * @param connectionId the connection id
     * @return {@code true} if the contents of the queue were deleted, or {@code false} if the process group or connection does not exist
     */
    boolean deleteQueue(@Nonnull String processGroupId, @Nonnull String connectionId);
}
