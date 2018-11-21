package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.context.SecurityContextUtil;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 *
 * TODO, why do we duplicate this logic
 * Can the MetadataClient for NiFi refer to something other than the /metadata root?
 * If so then this can go away and the MetadataClient can refer to the /catalog
 */
@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(CatalogMetadataController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class CatalogMetadataController extends AbstractCatalogController {

    private static final XLogger log = XLoggerFactory.getXLogger(CatalogMetadataController.class);

    public static final String BASE = "/v1/metadata/catalog";

    public enum CredentialMode {NONE, EMBED, ATTACH}

    @Inject
    AccessController accessController;

    @Inject
    DataSetProvider dataSetProvider;
    
    @Inject
    CatalogModelTransform modelTransform;

    @Inject
    MetadataAccess metadataService;

    @Inject
    private DataSourceCredentialManager credentialManager;


    @Inject
    private DataSourceProvider dataSourceProvider;


    @GET
    @Path("dataset/{id}")
    @ApiOperation("Retrieves the specified data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data set", response = DataSet.class),
                      @ApiResponse(code = 403, message = "Access denied", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data set not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response getDataSet(@PathParam("id") @UUID final String dataSetId) {
        log.entry(dataSetId);
        final boolean encryptCredentials = true;

        return metadataService
            .read(() -> {
                accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_DATASOURCES);
                
                com.thinkbiganalytics.metadata.api.catalog.DataSet.ID domainId = dataSetProvider.resolveId(dataSetId);
                
                return dataSetProvider.find(domainId)
                    .map(modelTransform.dataSetToRestModel(encryptCredentials))
                    .map(dataSet -> Response.ok(log.exit(dataSet)).build())
                    .orElseThrow(() -> {
                        log.debug("Data set not found: {}", dataSetId);
                        return new NotFoundException(getMessage("catalog.dataset.notFound"));
                    });
            });
    }


    @GET
    @ApiOperation("Gets the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source", response = DataSource.class),
                      @ApiResponse(code = 404, message = "Data source was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("datasource/{id}")
    public Response getDataSource(@PathParam("id") final String dataSourceId,
                                  @QueryParam("credentials") @DefaultValue("embed") final String credentialMode) {  // TODO Change default to be "none"
        log.entry(dataSourceId);
        final boolean encryptCredentials = true;
        CredentialMode mode;

        try {
            mode = CredentialMode.valueOf(credentialMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(log.exit(Response.Status.BAD_REQUEST)).entity(getMessage("catalog.datasource.credential.arg.invalid", credentialMode)).build();
        }

        final Set<Principal> principals = SecurityContextUtil.getCurrentPrincipals();
        DataSource dataSource = findDataSource(dataSourceId, encryptCredentials);  // TODO remove encrypt flag when real credential manager is in place

        switch (mode) {
            case NONE:
                dataSource = this.credentialManager.applyPlaceholders(dataSource, principals);
                break;
            case EMBED:
                dataSource = this.credentialManager.applyCredentials(dataSource, principals);
                break;
            case ATTACH:
                Map<String, String> credProps = this.credentialManager.getCredentials(dataSource, encryptCredentials, principals);
                DataSourceCredentials creds = new DataSourceCredentials(credProps, encryptCredentials);
                dataSource = this.credentialManager.applyPlaceholders(dataSource, principals);
                dataSource.setCredentials(creds);
        }

        return Response.ok(log.exit(dataSource)).build();
    }

    /**
     * Gets the data source with the specified id.
     *
     * @throws NotFoundException if the data source does not exist
     */
    @Nonnull
    private DataSource findDataSource(@Nonnull final String id, boolean encryptCredentials) {
        return metadataService.read(() -> {
            // Require admin permission if the results should include unencrypted credentials.
            accessController.checkPermission(AccessController.SERVICES, encryptCredentials ? FeedServicesAccessControl.ACCESS_DATASOURCES : FeedServicesAccessControl.ADMIN_DATASOURCES);

            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dsId = dataSourceProvider.resolveId(id);
            return dataSourceProvider.find(dsId)
                .map(modelTransform.dataSourceToRestModel(true, encryptCredentials))
                .orElseThrow(() -> {
                    log.debug("Data source not found: {}", id);
                    return new NotFoundException(getMessage("catalog.datasource.notFound.id", id));
                });
        });
    }

}
