package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.entity.InputPortEntity;
import org.apache.nifi.web.api.entity.OutputPortEntity;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiPortsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiPortsRestClientV0 implements NiFiPortsRestClient {

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiPortsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiPortsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public PortDTO updateInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        final InputPortEntity entity = new InputPortEntity();
        entity.setInputPort(inputPort);

        try {
            return client.put("/controller/process-groups/" + processGroupId + "/input-ports/" + inputPort.getId(), entity, InputPortEntity.class).getInputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO updateOutputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort) {
        final OutputPortEntity entity = new OutputPortEntity();
        entity.setOutputPort(outputPort);

        try {
            return client.put("/controller/process-groups/" + processGroupId + "/output-ports/" + outputPort.getId(), entity, OutputPortEntity.class).getOutputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }
}
