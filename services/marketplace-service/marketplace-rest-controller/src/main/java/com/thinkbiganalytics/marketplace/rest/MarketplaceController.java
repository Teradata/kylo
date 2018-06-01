package com.thinkbiganalytics.marketplace.rest;

/*-
 * #%L
 * marketplace-rest-controller
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

import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.marketplace.MarketplaceItemMetadata;
import com.thinkbiganalytics.marketplace.MarketplaceService;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Marketplace - File system", produces = "application/json")
@Component
@Path("/v1/marketplace")
@SwaggerDefinition(tags = @Tag(name = "Marketplace - File system", description = "templates"))
public class MarketplaceController {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceController.class);

    @Inject
    MarketplaceService filesystemMarketplaceService;

    @GET
    @Path("templates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("List all templates available for ingestion.")
    @ApiResponse(code = 200, message = "Returns templates of all types.")
    public List<MarketplaceItemMetadata> listTemplates() throws Exception {
        return filesystemMarketplaceService.listTemplates();
    }

    @POST
    @Path("templates/import")
    @ApiOperation("Imports selected templates.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importTemplate(List<String> templateFileNames)  throws Exception{
        log.info("importTemplate - begin");
        List<ImportTemplate> statusList = filesystemMarketplaceService.importTemplates(templateFileNames);
        log.info("importTemplate - Received response", statusList);
        return Response.ok(statusList).build();
    }

}
