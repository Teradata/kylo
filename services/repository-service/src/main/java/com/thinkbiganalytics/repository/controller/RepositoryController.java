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
import com.thinkbiganalytics.repository.api.TemplateMetadata;
import com.thinkbiganalytics.repository.api.RepositoryService;
import com.thinkbiganalytics.repository.api.TemplateMetadataWrapper;
import com.thinkbiganalytics.repository.api.TemplateRepository;
import com.thinkbiganalytics.repository.api.TemplateSearchFilter;
import com.thinkbiganalytics.rest.model.search.SearchResult;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all available repositories.")
    @ApiResponse(code = 200, message = "Returns available repositories.")
    public List<TemplateRepository> listRepositories() throws Exception {
        return repositoryService.listRepositories();
    }

    @GET
    @Path("{repositoryType}/{repositoryName}/templates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all templates available in the given repository.")
    @ApiResponse(code = 200, message = "Returns templates of all types.")
    public List<TemplateMetadataWrapper> listTemplatesByRepository(@NotBlank @PathParam("repositoryName") String repositoryName,
                                                                   @NotBlank @PathParam("repositoryType") String repositoryType) throws Exception {
        return repositoryService.listTemplatesByRepository(repositoryType, repositoryName);
    }

/*
    @GET
    @Path("templates")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all templates from all repositories.")
    @ApiResponse(code = 200, message = "Returns templates of all types.")
    public List<TemplateMetadataWrapper> listTemplates() throws Exception {
        return repositoryService.listTemplates();
    }
*/
    @GET
    @Path("template-page")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(hidden = true, value="Paginated response of available templates in all repositories.")
    @ApiResponse(code = 200, message = "Returns templates of all types.")
    public SearchResult getTemplatesPage(@QueryParam("sort") @DefaultValue("") String sort,
                                         @QueryParam("limit") @DefaultValue("10") Integer limit,
                                         @QueryParam("start") @DefaultValue("0") Integer start) {
        return repositoryService.getTemplatesPage(new TemplateSearchFilter(sort, limit, start));
    }

    @POST
    @Path("templates/import")
    @ApiOperation(hidden = true, value="Imports selected template.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importTemplate(ImportTemplateRequest req) throws Exception {
        log.info("Begin import {} into {}", req.getFileName(), req.getRepositoryName());
        ImportTemplate
            importStatus =
            repositoryService.importTemplate(req.getRepositoryName(),
                                             req.getRepositoryType(),
                                             req.getFileName(),
                                             req.getUploadKey(),
                                             req.getImportComponents());
        log.info("importTemplate - Received service response", importStatus);
        return Response.ok(importStatus).build();
    }

    @POST
    @Path("templates/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(hidden = true, value="Publishes template to the repository selected.")
    @ApiResponse(code = 200, message = "Successfully published template to repository.")
    public Response publishTemplate(@Valid PublishTemplateRequest req) throws Exception {

        TemplateMetadataWrapper templateMetadata = repositoryService
            .publishTemplate(req.getRepositoryName(),
                             req.getRepositoryType(),
                             req.getTemplateId(),
                             req.isOverwrite());
        return Response.ok(templateMetadata).build();
    }

    @GET
    @Path("{repositoryType}/{repositoryName}/{fileName}/templates/download/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation("Downloads template zip file from repository.")
    @ApiResponse(code = 200, message = "Successfully published template to repository.")
    public Response downloadTemplate(@NotBlank @PathParam("repositoryName") String repositoryName,
                                     @NotBlank @PathParam("repositoryType") String repositoryType,
                                     @NotBlank @PathParam("fileName") String fileName) throws Exception {

        byte[] fileData = repositoryService.downloadTemplate(repositoryName, repositoryType, fileName);
        return Response.ok(fileData, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachments; filename=\"" + fileName + "\"") //optional
            .build();
    }

}
