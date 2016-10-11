package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.nifi.web.api.entity.Entity;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;

/**
 * Implements a {@link NiFiRestClient} for communicating with NiFi v0.6.
 */
public class NiFiRestClientV0 extends JerseyRestClient implements NiFiRestClient {

    /** NiFi Process Groups REST client */
    @Nullable
    private NiFiProcessGroupsRestClientV0 processGroups;

    /** NiFi Templates REST client */
    @Nullable
    private NiFiTemplatesRestClientV0 templates;

    /**
     * Constructs a {@code NiFiRestClientV0} with the specified NiFi REST client configuration.
     *
     * @param config the NiFi REST client configuration
     */
    public NiFiRestClientV0(@Nonnull final NifiRestClientConfig config) {
        super(config);
    }

    @Nonnull
    @Override
    public NiFiProcessGroupsRestClient processGroups() {
        if (processGroups == null) {
            processGroups = new NiFiProcessGroupsRestClientV0(this);
        }
        return processGroups;
    }

    @Nonnull
    @Override
    public NiFiTemplatesRestClient templates() {
        if (templates == null) {
            templates = new NiFiTemplatesRestClientV0(this);
        }
        return templates;
    }

    Map<String, Object> getUpdateParams() {
        Entity status = getControllerRevision();
        Map<String, Object> params = new HashMap<>();
        params.put("version", status.getRevision().getVersion().toString());
        params.put("clientId", status.getRevision().getClientId());
        return params;
    }

    /**
     * Gets the current Revision and Version of Nifi instance. This is needed when performing an update to pass over the revision.getVersion() for locking purposes
     */
    Entity getControllerRevision() {
        return get("/controller/revision", null, Entity.class);
    }

    void updateEntityForSave(Entity entity) {
        Entity status = getControllerRevision();
        entity.setRevision(status.getRevision());
    }

    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }
}
