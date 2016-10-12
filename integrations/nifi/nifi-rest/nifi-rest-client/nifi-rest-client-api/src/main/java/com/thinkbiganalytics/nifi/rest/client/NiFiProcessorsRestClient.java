package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Processors REST endpoint as a Java class.
 */
public interface NiFiProcessorsRestClient {

    /**
     * Gets a processor.
     *
     * @param processGroupId the process group id
     * @param processorId the processor id
     * @return the processor, if found
     */
    @Nonnull
    Optional<ProcessorDTO> findById(@Nonnull String processGroupId, @Nonnull String processorId);

    /**
     * Updates a processor.
     *
     * @param processor the processor
     * @return the updated processor
     * @throws NifiComponentNotFoundException if the processor does not exist
     */
    @Nonnull
    ProcessorDTO update(@Nonnull ProcessorDTO processor);
}
