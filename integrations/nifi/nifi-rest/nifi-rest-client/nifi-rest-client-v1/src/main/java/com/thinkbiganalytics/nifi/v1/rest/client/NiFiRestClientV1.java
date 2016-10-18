package com.thinkbiganalytics.nifi.v1.rest.client;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.nifi.web.api.dto.AboutDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;

public class NiFiRestClientV1 extends JerseyRestClient implements NiFiRestClient {

    /** NiFi Connections REST client */
    @Nullable
    private NiFiConnectionsRestClientV1 connections;

    /** NiFi Controller Services REST client */
    @Nullable
    private NiFiControllerServicesRestClientV1 controllerServices;

    /** NiFi Ports REST client */
    @Nullable
    private NiFiPortsRestClientV1 ports;

    /** NiFi Process Groups REST client */
    @Nullable
    private NiFiProcessGroupsRestClientV1 processGroups;

    /** NiFi Processors REST client */
    @Nullable
    private NiFiProcessorsRestClientV1 processors;

    /** NiFi Templates REST client */
    @Nullable
    private NiFiTemplatesRestClientV1 templates;

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

    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }
}
