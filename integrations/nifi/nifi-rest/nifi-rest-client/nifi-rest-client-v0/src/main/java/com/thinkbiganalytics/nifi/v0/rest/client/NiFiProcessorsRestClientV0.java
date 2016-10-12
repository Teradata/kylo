package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessorEntity;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiProcessorsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiProcessorsRestClientV0 implements NiFiProcessorsRestClient {

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiProcessorsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessorsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ProcessorDTO> findById(@Nonnull final String processGroupId, @Nonnull final String processorId) {
        try {
            return Optional.of(client.get("/controller/process-groups/" + processGroupId + "/processors/" + processorId, null, ProcessorEntity.class).getProcessor());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public ProcessorDTO update(@Nonnull final ProcessorDTO processor) {
        final ProcessorEntity processorEntity = new ProcessorEntity();
        processorEntity.setProcessor(processor);

        try {
            return client.put("/controller/process-groups/" + processor.getParentGroupId() + "/processors/" + processor.getId(), processorEntity, ProcessorEntity.class).getProcessor();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processor.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESSOR, e);
        }
    }
}
