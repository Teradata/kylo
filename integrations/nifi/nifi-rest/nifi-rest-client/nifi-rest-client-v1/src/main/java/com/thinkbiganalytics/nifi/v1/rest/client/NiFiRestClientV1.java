package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.DefaultNiFiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRemoteProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiReportingTaskRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NiFiClusterSummary;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.BulletinBoardDTO;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ClusterSummaryDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.BulletinEntity;
import org.apache.nifi.web.api.entity.ClusteSummaryEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

public class NiFiRestClientV1 extends JerseyRestClient implements NiFiRestClient {

    /**
     * NiFi Connections REST client
     */
    @Nullable
    protected NiFiConnectionsRestClientV1 connections;

    /**
     * NiFi Controller Services REST client
     */
    @Nullable
    protected NiFiControllerServicesRestClientV1 controllerServices;

    /**
     * NiFi Ports REST client
     */
    @Nullable
    protected NiFiPortsRestClientV1 ports;

    /**
     * NiFi Process Groups REST client
     */
    @Nullable
    protected NiFiProcessGroupsRestClientV1 processGroups;

    /**
     * NiFi Processors REST client
     */
    @Nullable
    protected NiFiProcessorsRestClientV1 processors;

    /**
     * NiFi Templates REST client
     */
    @Nullable
    protected NiFiTemplatesRestClientV1 templates;

    /**
     * NiFi Flows REST client
     */
    @Nullable
    protected NiFiFlowVisitorClient flows;

    /**
     * Reporting tasks client
     */
    @Nullable
    protected NiFiReportingTaskRestClientV1 reportingTasks;


    protected NiFiRemoteProcessGroupsRestClientV1 remoteProcessGroups;

    /**
     * Constructs a {@code NiFiRestClientV1} with the specified NiFi REST client configuration.
     *
     * @param config the NiFi REST client configuration
     */
    public NiFiRestClientV1(@Nonnull final NifiRestClientConfig config) {
        super(config);
    }

    @Nonnull
    @Override
    public AboutDTO about() {
        return get("/flow/about", null, AboutEntity.class,false).getAbout();
    }

    @Nonnull
    @Override
    public NiFiClusterSummary clusterSummary() {
        final ClusterSummaryDTO dto = get("/flow/cluster/summary", null, ClusteSummaryEntity.class).getClusterSummary();
        final NiFiClusterSummary clusterSummary = new NiFiClusterSummary();
        clusterSummary.setClustered(dto.getClustered());
        clusterSummary.setConnectedNodeCount(dto.getConnectedNodeCount());
        clusterSummary.setConnectedNodes(dto.getConnectedNodes());
        clusterSummary.setConnectedToCluster(dto.getConnectedToCluster());
        clusterSummary.setTotalNodeCount(dto.getTotalNodeCount());
        return clusterSummary;
    }

    @Nonnull
    @Override
    public NiFiConnectionsRestClient connections() {
        if (connections == null) {
            connections = new NiFiConnectionsRestClientV1(this);
        }
        return connections;
    }

    @Nonnull
    @Override
    public NiFiControllerServicesRestClient controllerServices() {
        if (controllerServices == null) {
            controllerServices = new NiFiControllerServicesRestClientV1(this);
        }
        return controllerServices;
    }

    @Nonnull
    @Override
    public List<BulletinDTO> getBulletins(@Nonnull final String sourceId) {
        return Optional.ofNullable(get("/flow/bulletin-board", ImmutableMap.of("sourceId", sourceId), BulletinBoardEntity.class))
            .map(BulletinBoardEntity::getBulletinBoard)
            .map(BulletinBoardDTO::getBulletins)
            .map(bulletins -> bulletins.stream().map(BulletinEntity::getBulletin).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public List<BulletinDTO> getBulletinsMatchingSource(@Nonnull final String sourceIdRegexPattern,Long after) {
        Map<String,Object> params = null;
        if(after != null){
            params = ImmutableMap.of("sourceId", sourceIdRegexPattern,"after",after);
        }
        else {
            params = ImmutableMap.of("sourceId", sourceIdRegexPattern);
        }
        return Optional.ofNullable(get("/flow/bulletin-board", params, BulletinBoardEntity.class))
            .map(BulletinBoardEntity::getBulletinBoard)
            .map(BulletinBoardDTO::getBulletins)
            .map(bulletins -> bulletins.stream().map(BulletinEntity::getBulletin).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public List<BulletinDTO> getBulletinsMatchingMessage(@Nonnull String regexPattern) {
        return Optional.ofNullable(get("/flow/bulletin-board", ImmutableMap.of("message", regexPattern), BulletinBoardEntity.class))
            .map(BulletinBoardEntity::getBulletinBoard)
            .map(BulletinBoardDTO::getBulletins)
            .map(bulletins -> bulletins.stream().map(BulletinEntity::getBulletin).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public List<BulletinDTO> getBulletinsMatchingMessage(@Nonnull String regexPattern,Long afterId) {
        return Optional.ofNullable(get("/flow/bulletin-board", ImmutableMap.of("message", regexPattern,"after",afterId), BulletinBoardEntity.class))
            .map(BulletinBoardEntity::getBulletinBoard)
            .map(BulletinBoardDTO::getBulletins)
            .map(bulletins -> bulletins.stream().map(BulletinEntity::getBulletin).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }


    @Nonnull
    @Override
    public NiFiPortsRestClient ports() {
        if (ports == null) {
            ports = new NiFiPortsRestClientV1(this);
        }
        return ports;
    }

    @Nonnull
    @Override
    public NiFiProcessGroupsRestClient processGroups() {
        if (processGroups == null) {
            processGroups = new NiFiProcessGroupsRestClientV1(this);
        }
        return processGroups;
    }

    @Nonnull
    @Override
    public NiFiProcessorsRestClient processors() {
        if (processors == null) {
            processors = new NiFiProcessorsRestClientV1(this);
        }
        return processors;
    }

    @Nonnull
    @Override
    public SearchResultsDTO search(@Nonnull final String term) {
        return get("/flow/search-results", ImmutableMap.of("q", term), SearchResultsEntity.class).getSearchResultsDTO();
    }

    @Nonnull
    @Override
    public NiFiTemplatesRestClient templates() {
        if (templates == null) {
            templates = new NiFiTemplatesRestClientV1(this);
        }
        return templates;
    }

    @Nonnull
    @Override
    public NiFiFlowVisitorClient flows() {
        if (flows == null) {
            flows = new DefaultNiFiFlowVisitorClient(this);
        }
        return flows;
    }

    @Nonnull
    @Override
    public NiFiReportingTaskRestClient reportingTasks() {
        if (reportingTasks == null) {
            reportingTasks = new NiFiReportingTaskRestClientV1(this);
        }
        return reportingTasks;
    }

    @Nonnull
    @Override
    public NiFiRemoteProcessGroupsRestClient remoteProcessGroups() {
        if (remoteProcessGroups == null) {
            remoteProcessGroups = new NiFiRemoteProcessGroupsRestClientV1(this);
        }
        return remoteProcessGroups;
    }


    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }

    @Override
    protected void registerClientFeatures(Client client) {
        JacksonJsonProvider jacksonJsonProvider =
            new JacksonJaxbJsonProvider()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        client.register(jacksonJsonProvider);
        client.register(JacksonFeature.class);
    }
}
