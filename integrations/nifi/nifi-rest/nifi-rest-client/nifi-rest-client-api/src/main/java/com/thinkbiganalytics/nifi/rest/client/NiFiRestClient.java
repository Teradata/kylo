package com.thinkbiganalytics.nifi.rest.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Communicates with a NiFi service using a REST interface.
 */
public interface NiFiRestClient {

    /**
     * Gets the client for managing controller services and updating controller service references.
     *
     * @return the NiFi Controller Services client
     */
    @Nonnull
    NiFiControllerServicesRestClient controllerServices();

    /**
     * Gets the client for managing ports.
     *
     * @return the NiFi Ports client
     */
    @Nonnull
    NiFiPortsRestClient ports();

    /**
     * Gets the client for accessing process groups, including creating components, instantiating a template, and uploading a template.
     *
     * @return the NiFi Process Groups client
     */
    @Nonnull
    NiFiProcessGroupsRestClient processGroups();

    /**
     * Gets the client for downloading or deleting templates.
     *
     * @return the NiFi Templates client
     */
    @Nonnull
    NiFiTemplatesRestClient templates();
}
