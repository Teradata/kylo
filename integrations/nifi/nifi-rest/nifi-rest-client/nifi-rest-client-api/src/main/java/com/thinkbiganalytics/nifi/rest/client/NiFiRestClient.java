package com.thinkbiganalytics.nifi.rest.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Communicates with a NiFi service using a REST interface.
 */
public interface NiFiRestClient {

    /**
     * Gets the client for managing connections, including creating a connection, setting queue priority, and updating the connection destination.
     *
     * @return the NiFi Connections client
     */
    @Nonnull
    NiFiConnectionsRestClient connections();

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
     * Gets the client for creating a processor, setting properties, and scheduling.
     *
     * @return the NiFi Processors client
     */
    @Nonnull
    NiFiProcessorsRestClient processors();

    /**
     * Gets the client for downloading or deleting templates.
     *
     * @return the NiFi Templates client
     */
    @Nonnull
    NiFiTemplatesRestClient templates();
}
