package com.thinkbiganalytics.nifi.rest.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Communicates with a NiFi service using a REST interface.
 */
public interface NiFiRestClient {

    /**
     * Gets the client for downloading or deleting templates.
     *
     * @return the NiFi Templates client
     */
    @Nonnull
    NiFiTemplatesRestClient templates();
}
