package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
     * find bulletins matching a sourceId regex pattern optionally after a given bulletin dto id
     * @param sourceIdRegexPattern the regext pattern of the source ids
     * @param after optional id to query after
     * @return the bulletins
     */
    List<BulletinDTO> getBulletinsMatchingSource(@Nonnull final String sourceIdRegexPattern,Long after);

    /**
     * Gets the current bulletins with the message matching the supplied regex pattern
     *
     * @param regexPattern the regex pattern to matching against the Bulletin message
     * @return the bulletins
     */
    @Nonnull
    List<BulletinDTO> getBulletinsMatchingMessage(@Nonnull String regexPattern);


    /**
     * Gets the current bulletins with the message matching the supplied regex pattern
     * Looking for all messages after the passed in id
     *
     * @param regexPattern the regex pattern to matching against the Bulletin message
     * @param afterId the id to query after
     * @return the bulletins
     */
    @Nonnull
    List<BulletinDTO> getBulletinsMatchingMessage(@Nonnull String regexPattern, Long afterId);

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
     * Gets the client for accessing the remote process groups
     * @return
     */
    NiFiRemoteProcessGroupsRestClient remoteProcessGroups();

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
     */
    @Nonnull
    NiFiFlowVisitorClient flows();

    /**
     * Gets the client that will manage NiFi reporting tasks
     *
     * @return the reporting task client
     */
    @Nonnull
    NiFiReportingTaskRestClient reportingTasks();


}
