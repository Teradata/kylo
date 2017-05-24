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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.DefaultNiFiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiFlowVisitorClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;

public class NiFiRestClientV1 extends JerseyRestClient implements NiFiRestClient {

    /**
     * NiFi Connections REST client
     */
    @Nullable
    private NiFiConnectionsRestClientV1 connections;

    /**
     * NiFi Controller Services REST client
     */
    @Nullable
    private NiFiControllerServicesRestClientV1 controllerServices;

    /**
     * NiFi Ports REST client
     */
    @Nullable
    private NiFiPortsRestClientV1 ports;

    /**
     * NiFi Process Groups REST client
     */
    @Nullable
    private NiFiProcessGroupsRestClientV1 processGroups;

    /**
     * NiFi Processors REST client
     */
    @Nullable
    private NiFiProcessorsRestClientV1 processors;

    /**
     * NiFi Templates REST client
     */
    @Nullable
    private NiFiTemplatesRestClientV1 templates;

    /**
     * NiFi Flows REST client
     */
    @Nullable
    private NiFiFlowVisitorClient flows;

    /**
     * Reporting tasks client
     */
    @Nullable
    private NiFiReportingTaskRestClientV1 reportingTasks;

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
        return get("/flow/about", null, AboutEntity.class).getAbout();
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
    public List<BulletinDTO> getBulletinsMatchingMessage(@Nonnull String regexPattern) {
        return Optional.ofNullable(get("/flow/bulletin-board", ImmutableMap.of("message", regexPattern), BulletinBoardEntity.class))
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
            return new NiFiReportingTaskRestClientV1(this);
        }
        return reportingTasks;
    }


    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }
}
