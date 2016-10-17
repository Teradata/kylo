package com.thinkbiganalytics.nifi.rest.client;

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

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
            deleteQueueWithRetries(processGroupId, connectionId, 3, 300, TimeUnit.MILLISECONDS);
            return true;
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
     * Sends a request to drop the contents of a queue and waits for it to finish.
     *
     * @param processGroupId the process group id
     * @param connectionId the connection id
     * @param retries number of retries, at least 0; will try {@code retries} + 1 times
     * @param timeout duration to wait between retries
     * @param timeUnit unit of time for {@code timeout}
     * @return the drop request, if finished
     * @throws NifiClientRuntimeException if the operation times out
     * @throws NifiComponentNotFoundException if the process group or connection does not exist
     */
    protected DropRequestDTO deleteQueueWithRetries(@Nonnull final String processGroupId, @Nonnull final String connectionId, final int retries, final int timeout, @Nonnull final TimeUnit timeUnit) {
        // Request queue drop
        DropRequestDTO dropRequest = createDropRequest(processGroupId, connectionId);

        // Wait for finished
        for (int count=0; !dropRequest.isFinished() && count < retries; ++count) {
            Uninterruptibles.sleepUninterruptibly(timeout, timeUnit);
            dropRequest = getDropRequest(processGroupId, connectionId, dropRequest.getId())
                    .orElseThrow(() -> new NifiComponentNotFoundException(connectionId, NifiConstants.NIFI_COMPONENT_TYPE.CONNECTION, null));
        }

        if (!dropRequest.isFinished()) {
            throw new NifiClientRuntimeException("Timeout waiting for queue to delete for connection: " + connectionId);
        }

        // Cleanup
        return deleteDropRequest(processGroupId, connectionId, dropRequest.getId())
                .orElseThrow(() -> new NifiComponentNotFoundException(connectionId, NifiConstants.NIFI_COMPONENT_TYPE.CONNECTION, null));
    }

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
