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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * REST API for administrative functions.
 */
@Api(tags = "Feed Manager - Administration", produces = "application/json")
@Path(AdminControllerV2.BASE)
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Administration", description = "administrator operations"))
public class AdminControllerV2 {

    public static final String BASE = "/v2/feedmgr/admin";
    public static final String IMPORT_TEMPLATE = "/import-template";
    public static final String IMPORT_FEED = "/import-feed";

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @Inject
    ExportImportFeedService exportImportFeedService;

    @Inject
    UploadProgressService uploadProgressService;

    /**
     * Feed manager metadata service
     */
    @Inject
    MetadataService metadataService;

    @GET
    @Path("/upload-status/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets thet status of a given upload/import.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the upload status")
                  })
    public Response uploadStatus(@NotNull @PathParam("key") String key) {
        UploadProgress uploadProgress = uploadProgressService.getUploadStatus(key);
        if (uploadProgress != null) {
            uploadProgress.checkAndIncrementPercentage();
            return Response.ok(uploadProgress).build();
        } else {
            return Response.ok(uploadProgress).build();
        }
    }


    @POST
    @Path(IMPORT_FEED)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Imports a feed zip file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed metadata.", response = ExportImportFeedService.ImportFeed.class),
                      @ApiResponse(code = 500, message = "There was a problem importing the feed.", response = RestResponseStatus.class)
                  })
    public Response uploadFeed(@NotNull @FormDataParam("file") InputStream fileInputStream,
                               @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @NotNull @FormDataParam("uploadKey") String uploadKey,
                               @FormDataParam("categorySystemName") String categorySystemName,
                               @FormDataParam("disableFeedUponImport") @DefaultValue("false") boolean disableFeedUponImport,
                               @FormDataParam("importComponents") String importComponents)
        throws Exception {
        ImportFeedOptions options = new ImportFeedOptions();
        options.setUploadKey(uploadKey);
        options.setDisableUponImport(disableFeedUponImport);
        ExportImportFeedService.ImportFeed importFeed = null;

        options.setCategorySystemName(categorySystemName);

        boolean overwriteFeed = true;
        boolean overwriteTemplate = true;
        uploadProgressService.newUpload(uploadKey);

        if (importComponents == null) {
            byte[] content = ImportUtil.streamToByteArray(fileInputStream);
            importFeed = exportImportFeedService.validateFeedForImport(fileMetaData.getFileName(), content, options);
            importFeed.setSuccess(false);
        } else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {
            }));
            byte[] content = ImportUtil.streamToByteArray(fileInputStream);
            importFeed = exportImportFeedService.importFeed(fileMetaData.getFileName(), content, options);
        }
        uploadProgressService.removeUpload(uploadKey);
        return Response.ok(importFeed).build();
    }

    @POST
    @Path(IMPORT_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Imports a template xml or zip file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the template metadata.", response = ExportImportTemplateService.ImportTemplate.class),
                      @ApiResponse(code = 500, message = "There was a problem importing the template.", response = RestResponseStatus.class)
                  })
    public Response uploadTemplate(@NotNull @FormDataParam("file") InputStream fileInputStream,
                                   @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                   @NotNull @FormDataParam("uploadKey") String uploadKey,
                                   @FormDataParam("importComponents") String importComponents)
        throws Exception {
        ImportTemplateOptions options = new ImportTemplateOptions();
        options.setUploadKey(uploadKey);
        ExportImportTemplateService.ImportTemplate importTemplate = null;
        byte[] content = ImportUtil.streamToByteArray(fileInputStream);

        uploadProgressService.newUpload(uploadKey);

        if (importComponents == null) {
            importTemplate = exportImportTemplateService.validateTemplateForImport(fileMetaData.getFileName(), content, options);
            importTemplate.setSuccess(false);
        } else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {
            }));
            importTemplate = exportImportTemplateService.importTemplate(fileMetaData.getFileName(), content, options);
        }
        return Response.ok(importTemplate).build();
    }

}
