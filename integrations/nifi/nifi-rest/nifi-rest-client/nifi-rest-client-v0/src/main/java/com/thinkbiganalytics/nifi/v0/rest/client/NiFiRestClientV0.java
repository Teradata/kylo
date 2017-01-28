package com.thinkbiganalytics.nifi.v0.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v0
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
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.BulletinBoardEntity;
import org.apache.nifi.web.api.entity.Entity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

/**
 * Implements a {@link NiFiRestClient} for communicating with NiFi v0.6.
 */
public class NiFiRestClientV0 extends JerseyRestClient implements NiFiRestClient {

    /** Parameter name for the revision client ID */
    private static final String CLIENT_ID = "clientId";

    /** Parameter name for the revision version */
    private static final String VERSION = "version";

    /** NiFi REST client configuration */
    @Nonnull
    private final NifiRestClientConfig clientConfig;

    /** NiFi Connections REST client */
    @Nullable
    private NiFiConnectionsRestClientV0 connections;

    /** NiFi Controller Services REST client */
    @Nullable
    private NiFiControllerServicesRestClientV0 controllerServices;

    /** NiFi Ports REST client */
    @Nullable
    private NiFiPortsRestClientV0 ports;

    /** NiFi Process Groups REST client */
    @Nullable
    private NiFiProcessGroupsRestClientV0 processGroups;

    /** NiFi Processors REST client */
    @Nullable
    private NiFiProcessorsRestClientV0 processors;

    /** NiFi Templates REST client */
    @Nullable
    private NiFiTemplatesRestClientV0 templates;

    /** NiFi Flows REST client */
    @Nullable
    private NiFiFlowVisitorClient flows;

    /**
     * NiFi reporting task client
     */
    @Nullable
    private NiFiReportingTaskRestClient reportingTasks;

    /**
     * Constructs a {@code NiFiRestClientV0} with the specified NiFi REST client configuration.
     *
     * @param config the NiFi REST client configuration
     */
    public NiFiRestClientV0(@Nonnull final NifiRestClientConfig config) {
        super(config);
        clientConfig = config;
    }

    @Nonnull
    @Override
    public AboutDTO about() {
        return get("/controller/about", null, AboutEntity.class).getAbout();
    }

    @Nonnull
    @Override
    public NiFiClusterSummary clusterSummary() {
        final NiFiClusterSummary clusterSummary = new NiFiClusterSummary();
        clusterSummary.setClustered(false);
        clusterSummary.setConnectedToCluster(false);
        clusterSummary.setConnectedNodeCount(0);
        clusterSummary.setTotalNodeCount(0);
        return clusterSummary;
    }

    @Nonnull
    @Override
    public NiFiConnectionsRestClient connections() {
        if (connections == null) {
            connections = new NiFiConnectionsRestClientV0(this);
        }
        return connections;
    }

    @Nonnull
    @Override
    public NiFiControllerServicesRestClient controllerServices() {
        if (controllerServices == null) {
            controllerServices = new NiFiControllerServicesRestClientV0(this);
        }
        return controllerServices;
    }

    @Nonnull
    @Override
    public <T> T delete(@Nonnull final String path, @Nullable final Map<String, Object> params, @Nonnull final Class<T> returnType) {
        if (params != null) {
            final RevisionDTO revision = getRevision();
            params.put(CLIENT_ID, revision.getClientId());
            params.put(VERSION, revision.getVersion());
        }
        return super.delete(path, params, returnType);
    }

    @Nonnull
    @Override
    public List<BulletinDTO> getBulletins(@Nonnull final String sourceId) {
        return Optional.ofNullable(get("/controller/bulletin-board", ImmutableMap.of("sourceId", sourceId), BulletinBoardEntity.class))
                .map(BulletinBoardEntity::getBulletinBoard)
                .map(BulletinBoardDTO::getBulletins)
                .orElse(Collections.emptyList());
    }

    /**
     * Gets the NiFi cluster type.
     *
     * @return the NiFi cluster type
     */
    public String getClusterType() {
        return clientConfig.getClusterType();
    }

    @Nonnull
    @Override
    public NiFiPortsRestClient ports() {
        if (ports == null) {
            ports = new NiFiPortsRestClientV0(this);
        }
        return ports;
    }

    @Nonnull
    @Override
    public <T> T post(@Nonnull final String path, @Nullable final Object object, @Nonnull final Class<T> returnType) {
        return super.post(path, updateEntityForSave(object), returnType);
    }

    @Nonnull
    @Override
    public <T> T postForm(@Nonnull final String path, @Nullable final Form form, @Nonnull final Class<T> returnType) {
        if (form != null) {
            final RevisionDTO revision = getRevision();
            form.param(CLIENT_ID, revision.getClientId());
            form.param(VERSION, revision.getVersion().toString());
        }
        return super.postForm(path, form, returnType);
    }

    @Nonnull
    @Override
    public <T> T put(@Nonnull final String path, @Nullable final Object object, @Nonnull final Class<T> returnType) {
        return super.put(path, updateEntityForSave(object), returnType);
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
    public NiFiProcessorsRestClient processors() {
        if (processors == null) {
            processors = new NiFiProcessorsRestClientV0(this);
        }
        return processors;
    }

    @Nonnull
    @Override
    public SearchResultsDTO search(@Nonnull final String term) {
        return get("/controller/search-results", ImmutableMap.of("q", term), SearchResultsEntity.class).getSearchResultsDTO();
    }

    @Nonnull
    @Override
    public NiFiTemplatesRestClient templates() {
        if (templates == null) {
            templates = new NiFiTemplatesRestClientV0(this);
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


    /**
     * Reporting task interaction is not supported for 0.x
     *
     * @return a null client (not supported)
     */
    @Nonnull
    @Override
    public NiFiReportingTaskRestClient reportingTasks() {
        if (reportingTasks == null) {
            return new NiFiReportingTaskClientV0();
        }
        return reportingTasks;
    }

    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }

    /**
     * Gets the current Revision and Version of Nifi instance. This is needed when performing an update to pass over the revision.getVersion() for locking purposes
     */
    private RevisionDTO getRevision() {
        return get("/controller/revision", null, Entity.class).getRevision();
    }

    /**
     * Updates the specified REST entity for a {@code POST} or {@code PUT} operation.
     *
     * @param object the entity
     * @return the entity
     */
    private Object updateEntityForSave(@Nullable final Object object) {
        if (object instanceof Entity) {
            ((Entity)object).setRevision(getRevision());
        }
        return object;
    }
}
