package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;

/**
 * Implements a {@link NiFiRestClient} for communicating with NiFi v0.6.
 */
public class NiFiRestClientV0 extends JerseyRestClient implements NiFiRestClient {

    /** NiFi Templates rest client */
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
    public NiFiTemplatesRestClient templates() {
        if (templates == null) {
            templates = new NiFiTemplatesRestClientV0(this);
        }
        return templates;
    }

    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }
}
