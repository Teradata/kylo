package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.rest.model.DomainType;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.domaintype.DomainTypeTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.domaintype.DomainTypeProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * A REST interface for managing domain types.
 */
@Api(tags = "Feed Manager - Domain Types", produces = "application/json")
@Path("/v1/feedmgr/domain-types")
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Domain Types", description = "manages domain types"))
public class DomainTypesController {

    /**
     * Ensures the user has the correct permissions.
     */
    @Inject
    private AccessController accessController;

    /**
     * Provides access to domain types.
     */
    @Inject
    private DomainTypeProvider domainTypeProvider;

    /**
     * Transforms domain types between rest and domain models.
     */
    @Inject
    private DomainTypeTransform domainTypeTransform;

    /**
     * Provides access to metadata store.
     */
    @Inject
    private MetadataAccess metadata;

    @DELETE
    @Path("{id}")
    @ApiOperation("Deletes a domain type by id.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The domain type is deleted."),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public void deleteDomainType(@PathParam("id") final String id) {
        metadata.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

            domainTypeProvider.deleteById(domainTypeProvider.resolveId(id));
        });
    }

    @GET
    @Path("{id}")
    @ApiOperation("Gets a domain type by id.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the domain type.", response = DomainType.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The domain type does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public DomainType getDomainType(@PathParam("id") final String id) {
        return metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            final com.thinkbiganalytics.metadata.api.domaintype.DomainType domainModel = domainTypeProvider.findById(domainTypeProvider.resolveId(id));
            if (domainModel != null) {
                return domainTypeTransform.toRestModel(domainModel);
            } else {
                throw new NotFoundException();
            }
        });
    }

    @GET
    @ApiOperation("Gets all domain types.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the domain types.", response = DomainType.class, responseContainer = "List"),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public List<DomainType> getDomainTypes() {
        return metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            return domainTypeProvider.findAll().stream()
                .map(domainModel -> domainTypeTransform.toRestModel(domainModel))
                .collect(Collectors.toList());
        });
    }

    @POST
    @ApiOperation("Updates a domain type.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the domain type.", response = DomainType.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public DomainType updateDomainType(@Nonnull final DomainType restModel) {
        return metadata.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

            final com.thinkbiganalytics.metadata.api.domaintype.DomainType domainModel = domainTypeTransform.toDomainModel(restModel);
            domainTypeProvider.update(domainModel);
            return domainTypeTransform.toRestModel(domainModel);
        });
    }
}
