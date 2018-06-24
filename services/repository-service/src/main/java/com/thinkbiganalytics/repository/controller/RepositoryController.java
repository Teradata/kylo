package com.thinkbiganalytics.repository.controller;

/*-
 * #%L
 * kylo-repository-service
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
import com.thinkbiganalytics.repository.api.RepositoryItemMetadata;
import com.thinkbiganalytics.repository.api.RepositoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Repository - File system", produces = "application/json")
@Component
@Path("/v1/repository")
@SwaggerDefinition(tags = @Tag(name = "Repository - File system", description = "templates"))
public class RepositoryController {

    private static final Logger log = LoggerFactory.getLogger(RepositoryController.class);

    @Inject
    RepositoryService repositoryService;

    @GET
    @Path("templates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all templates available in repository.")
    @ApiResponse(code = 200, message = "Returns templates of all types.")
    public List<RepositoryItemMetadata> listTemplates() throws Exception {
        return repositoryService.listTemplates();
    }

    @POST
    @Path("templates/import")
    @ApiOperation("Imports selected templates.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importTemplate(RepositoryRequest request) throws Exception {
        log.info("import - begin {}, {}, {}", request.getFileName(), request.getUploadKey(), request.getImportComponents());
        ImportTemplate importStatus = repositoryService.importTemplates(request.getFileName(), request.getUploadKey(), request.getImportComponents());
        log.info("import - Received service response", importStatus);
        return Response.ok(importStatus).build();
    }

    @GET
    @Path("templates/publish/{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Publishes template to repository.")
    @ApiResponse(code = 200, message = "Successfully published template to repository.")
    public Response publishTemplate(@NotNull @PathParam("templateId") String templateId, @NotNull @QueryParam("overwrite") boolean overwrite) throws Exception {

        RepositoryItemMetadata metadata = repositoryService.publishTemplate(templateId, overwrite);
        return Response.ok(metadata).build();
    }

    @GET
    @Path("templates/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation("Downloads Template zip file from repository.")
    @ApiResponse(code = 200, message = "Successfully published template to repository.")
    public Response downloadTemplate(@NotNull @PathParam("fileName") String fileName) throws Exception {

        byte[] fileData = repositoryService.downloadTemplate(fileName);
        return Response.ok(fileData, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachments; filename=\"" + fileName + "\"") //optional
            .build();
    }

}
