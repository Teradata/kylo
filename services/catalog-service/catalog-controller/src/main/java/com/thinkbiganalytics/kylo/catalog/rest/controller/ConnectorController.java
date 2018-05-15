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

import com.thinkbiganalytics.kylo.catalog.connector.ConnectorProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(ConnectorController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectorController {

    public static final String BASE = "/v1/catalog/connector";

    private static final MessageSource MESSAGES;

    static {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("ConnectorMessages");
        MESSAGES = messageSource;
    }

    @Inject
    ConnectorProvider connectorProvider;

    @Inject
    HttpServletRequest request;

    @GET
    @ApiOperation("Gets the specified connector")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connector", response = Connector.class),
                      @ApiResponse(code = 404, message = "Connector was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getConnector(@PathParam("id") final String connectorId) {
        final Connector connector = connectorProvider.getConnector(connectorId).orElseThrow(() -> new BadRequestException(getMessage("notFound")));
        return Response.ok(connector).build();
    }

    @GET
    @ApiOperation("Lists all connectors")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connectors", response = Connector.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response listConnectors() {
        return Response.ok(connectorProvider.getConnectors()).build();
    }

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return MESSAGES.getMessage(code, null, RequestContextUtils.getLocale(request));
    }
}
