package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.nifi.rest.model.NiFiClusterSummary;

import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Communicates with a NiFi service using a REST interface.
 */
public interface NiFiRestClient {

    /**
     * Retrieves details about the NiFi service.
     *
     * @return about NiFi details
     */
    @Nonnull
    AboutDTO about();

    /**
     * Gets the cluster status for the NiFi.
     *
     * @return cluster summary
     */
    @Nonnull
    NiFiClusterSummary clusterSummary();

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
     * Gets the current bulletins for the specified source.
     *
     * @param sourceId the source id
     * @return the bulletins
     */
    @Nonnull
    List<BulletinDTO> getBulletins(@Nonnull String sourceId);

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
     * Performs a search against the NiFi service using the specified search term.
     *
     * @param term the search term
     * @return the search results
     */
    @Nonnull
    SearchResultsDTO search(@Nonnull String term);

    /**
     * Gets the client for downloading or deleting templates.
     *
     * @return the NiFi Templates client
     */
    @Nonnull
    NiFiTemplatesRestClient templates();


    /**
     * Gets the client that will walk the NiFi flow graph of processors starting with a specified Process Group
     * @return
     */
    @Nonnull
    NiFiFlowVisitorClient flows();
}
