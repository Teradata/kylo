package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.DropRequestEntity;

import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiConnectionsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiConnectionsRestClientV0 extends AbstractNiFiConnectionsRestClient {

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiConnectionsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiConnectionsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ConnectionDTO> delete(@Nonnull final String processGroupId, @Nonnull final String connectionId) {
        try {
            client.delete("/controller/process-groups/" + processGroupId + "/connections/" + connectionId, new HashMap<>(), ConnectionEntity.class);
            return Optional.of(new ConnectionDTO());
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    protected DropRequestDTO createDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId) {
        try {
            return client.post("/controller/process-groups/" + processGroupId + "/connections/" + connectionId + "/drop-requests", null, DropRequestEntity.class).getDropRequest();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException("Connection does not exist: " + connectionId, NifiConstants.NIFI_COMPONENT_TYPE.CONNECTION, e);
        }
    }

    @Nonnull
    @Override
    protected Optional<DropRequestDTO> deleteDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId, @Nonnull final String dropRequestId) {
        try {
            client.delete("/controller/process-groups/" + processGroupId + "/connections/" + connectionId + "/drop-requests/" + dropRequestId, null, DropRequestEntity.class);
            return Optional.of(new DropRequestDTO());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    protected Optional<DropRequestDTO> getDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId, @Nonnull final String dropRequestId) {
        try {
            final DropRequestEntity dropRequest = client.get("/controller/process-groups/" + processGroupId + "/connections/" + connectionId + "/drop-requests/" + dropRequestId, null,
                                                             DropRequestEntity.class);
            return Optional.of(dropRequest.getDropRequest());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
