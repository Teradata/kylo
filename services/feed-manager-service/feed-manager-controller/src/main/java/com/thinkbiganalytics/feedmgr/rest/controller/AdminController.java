package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UserFieldCollection;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST API for administrative functions.
 */
@Api(tags = "Feed Manager: Administration", produces = "application/json")
@Path("/v1/feedmgr/admin")
public class AdminController {

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @Inject
    ExportImportFeedService exportImportFeedService;

    /** Feed manager metadata service */
    @Inject
    MetadataService metadataService;

    @GET
    @Path("/export-template/{templateId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportTemplate(@NotNull @Size(min = 36, max = 36, message = "Invalid templateId size")
                                   @PathParam("templateId") String templateId) {
        ExportImportTemplateService.ExportTemplate zipFile = exportImportTemplateService.exportTemplate(templateId);
        return Response.ok(zipFile.getFile(), MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachments; filename=\"" + zipFile.getFileName() + "\"") //optional
            .build();
    }

    @GET
    @Path("/export-feed/{feedId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportFeed(@NotNull @Size(min = 36, max = 36, message = "Invalid feedId size")
                                   @PathParam("feedId") String feedId) {
        try {
            ExportImportFeedService.ExportFeed zipFile = exportImportFeedService.exportFeed(feedId);
            return Response.ok(zipFile.getFile(), MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachments; filename=\"" + zipFile.getFileName() + "\"") //optional
                .build();
        }catch (IOException e){
            throw new RuntimeException("Unable to export Feed "+e.getMessage());
        }
    }

    @POST
    @Path("/import-feed")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadFeed(@NotNull @FormDataParam("file") InputStream fileInputStream,
                                   @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                   @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                                   @FormDataParam("categorySystemName") String categorySystemName,
                               @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportOptions.IMPORT_CONNECTING_FLOW importConnectingFlow)
        throws Exception {
        ImportOptions options = new ImportOptions();
        options.setOverwrite(overwrite);
        options.setImportConnectingFlow(importConnectingFlow);
        options.setCategorySystemName(categorySystemName);
        ExportImportFeedService.ImportFeed importFeed = exportImportFeedService.importFeed(fileMetaData.getFileName(), fileInputStream, options);

        return Response.ok(importFeed).build();
    }

    @POST
    @Path("/import-template")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadTemplate(@NotNull @FormDataParam("file") InputStream fileInputStream,
                                  @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                  @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                                   @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportOptions.IMPORT_CONNECTING_FLOW importConnectingFlow,
                                  @FormDataParam("createReusableFlow") @DefaultValue("false") boolean createReusableFlow)
        throws Exception {
        ImportOptions options = new ImportOptions();
        options.setCreateReusableFlow(createReusableFlow);
        options.setOverwrite(overwrite);
        options.setImportConnectingFlow(importConnectingFlow);
        ExportImportTemplateService.ImportTemplate importTemplate = exportImportTemplateService.importTemplate(fileMetaData.getFileName(), fileInputStream, options);

        return Response.ok(importTemplate).build();
    }

    /**
     * Gets the user-defined fields for all categories and feeds.
     *
     * @return the user-defined fields
     * @since 0.4.0
     */
    @GET
    @Path("user-fields")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the user-defined fields for categories and feeds.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the user-defined fields.", response = UserFieldCollection.class),
            @ApiResponse(code = 500, message = "There was a problem accessing the user-defined fields.")
    })
    @Nonnull
    public Object getUserFields() {
        return metadataService.getUserFields();
    }

    /**
     * Sets the user-defined fields for all categories and feeds.
     *
     * @param userFields the new user-defined fields
     * @since 0.4.0
     */
    @POST
    @Path("user-fields")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sets the user-defined fields for categories and feeds.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The user-defined fields have been updated."),
            @ApiResponse(code = 500, message = "There was a problem updating the user-defined fields.")
    })
    public void setUserFields(@Nonnull final UserFieldCollection userFields) {
        metadataService.setUserFields(userFields);
    }
}
