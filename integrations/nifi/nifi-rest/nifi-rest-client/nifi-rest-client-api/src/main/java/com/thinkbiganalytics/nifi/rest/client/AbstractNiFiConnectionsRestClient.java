package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.nifi.web.api.dto.DropRequestDTO;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Provides a standard implementation of {@link NiFiConnectionsRestClient} that can be extended for different NiFi versions.
 */
public abstract class AbstractNiFiConnectionsRestClient implements NiFiConnectionsRestClient {

    @Override
    public boolean deleteQueue(@Nonnull final String processGroupId, @Nonnull final String connectionId) {
        try {
            // Drop the queue contents
            DropRequestDTO dropRequest = createDropRequest(processGroupId, connectionId);

            while (!dropRequest.isFinished()) {
                Uninterruptibles.sleepUninterruptibly(300L, TimeUnit.MILLISECONDS);

                // Get status
                final Optional<DropRequestDTO> update = getDropRequest(processGroupId, connectionId, dropRequest.getId());
                if (update.isPresent()) {
                    dropRequest = update.get();
                } else {
                    return false;
                }
            }

            // Cleanup
            final Optional<DropRequestDTO> delete = deleteDropRequest(processGroupId, connectionId, dropRequest.getId());
            return delete.isPresent();
        } catch (final NifiComponentNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a request to drop the contents of the queue for the specified connection.
     *
     * @param processGroupId the process group id
     * @param connectionId the connection id
     * @return the drop request
     * @throws NifiComponentNotFoundException if the process group or connection does not exist
     */
    @Nonnull
    protected abstract DropRequestDTO createDropRequest(@Nonnull String processGroupId, @Nonnull String connectionId);

    /**
     * Cancels and/or removes the specified request to drop the contents of a connection.
     *
     * @param processGroupId the process group id
     * @param connectionId the connection id
     * @param dropRequestId the drop request id
     * @return the drop request, if found
     */
    @Nonnull
    protected abstract Optional<DropRequestDTO> deleteDropRequest(@Nonnull String processGroupId, @Nonnull String connectionId, @Nonnull String dropRequestId);

    /**
     * Gets the current status of the specified drop request for a connection.
     *
     * @param processGroupId the process group id
     * @param connectionId the connection id
     * @param dropRequestId the drop request id
     * @return the drop request, if found
     */
    @Nonnull
    protected abstract Optional<DropRequestDTO> getDropRequest(@Nonnull String processGroupId, @Nonnull String connectionId, @Nonnull String dropRequestId);
}
