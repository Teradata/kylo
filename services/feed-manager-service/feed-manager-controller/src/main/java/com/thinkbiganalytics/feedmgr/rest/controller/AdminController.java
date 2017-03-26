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
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UserFieldCollection;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

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
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * REST API for administrative functions.
 */
@Api(tags = "Feed Manager - Administration", produces = "application/json")
@Path(AdminController.BASE)
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Administration", description = "administrator operations"))
public class AdminController {

    public static final String BASE = "/v1/feedmgr/admin";
    public static final String IMPORT_TEMPLATE = "/import-template";
    public static final String IMPORT_FEED = "/import-feed";

    public static final String IMPORT_TEMPLATE_NEW = "/import-template2";
    public static final String IMPORT_FEED_NEW = "/import-feed2";

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
    @Path("/export-template/{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Exports the template with the specified ID.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the template as an attachment."),
                      @ApiResponse(code = 400, message = "the templateId is invalid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The template is not available.", response = RestResponseStatus.class)
                  })
    public Response exportTemplate(@NotNull @Size(min = 36, max = 36, message = "Invalid templateId size")
                                   @PathParam("templateId") String templateId) {
        ExportImportTemplateService.ExportTemplate zipFile = exportImportTemplateService.exportTemplate(templateId);
        return Response.ok(zipFile.getFile(), MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachments; filename=\"" + zipFile.getFileName() + "\"") //optional
            .build();
    }

    @GET
    @Path("/export-feed/{feedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Exports the feed with the specified ID.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed as an attachment."),
                      @ApiResponse(code = 400, message = "The feedId is invalid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed is not available.", response = RestResponseStatus.class)
                  })
    public Response exportFeed(@NotNull @Size(min = 36, max = 36, message = "Invalid feedId size")
                               @PathParam("feedId") String feedId) {
        try {
            ExportImportFeedService.ExportFeed zipFile = exportImportFeedService.exportFeed(feedId);
            return Response.ok(zipFile.getFile(), MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachments; filename=\"" + zipFile.getFileName() + "\"") //optional
                .build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to export Feed " + e.getMessage());
        }
    }

    @GET
    @Path("/upload-status/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets thet status of a given upload/import.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the upload status")
                  })
    public Response uploadStatus(@NotNull @PathParam("key") String key) {
            UploadProgress uploadProgress = uploadProgressService.getUploadStatus(key);
            if(uploadProgress != null) {
                uploadProgress.checkAndIncrementPercentage();
                return Response.ok(uploadProgress).build();
            }
            else {
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
                               @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                               @FormDataParam("overwriteFeedTemplate") @DefaultValue("false") boolean overwriteFeedTemplate,
                               @FormDataParam("categorySystemName") String categorySystemName,
                               @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportTemplateOptions.IMPORT_CONNECTING_FLOW importConnectingFlow)
        throws Exception {
        ImportFeedOptions options = new ImportFeedOptions();
        options.findImportComponentOption(ImportComponent.FEED_DATA).setOverwrite(overwrite);
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(overwriteFeedTemplate);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(overwriteFeedTemplate);

        options.setCategorySystemName(categorySystemName);


        byte[] content = ImportUtil.streamToByteArray(fileInputStream);
        ExportImportFeedService.ImportFeed importFeed = exportImportFeedService.importFeed(fileMetaData.getFileName(), content, options);

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
    public Response uploadTemplatex(@NotNull @FormDataParam("file") InputStream fileInputStream,
                                    @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                    @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                                    @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportTemplateOptions.IMPORT_CONNECTING_FLOW importConnectingFlow,
                                    @FormDataParam("createReusableFlow") @DefaultValue("false") boolean createReusableFlow)
        throws Exception {

        ImportTemplateOptions options = new ImportTemplateOptions();
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(overwrite);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(overwrite);
        options.setCreateReusableFlow(createReusableFlow);

        byte[] content = ImportUtil.streamToByteArray(fileInputStream);
        ExportImportTemplateService.ImportTemplate importTemplate = exportImportTemplateService.importTemplate(fileMetaData.getFileName(), content, options);

        return Response.ok(importTemplate).build();
    }




    @POST
    @Path(IMPORT_FEED_NEW)
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
                               @FormDataParam("importComponents") String importComponents)
        throws Exception {
        ImportFeedOptions options = new ImportFeedOptions();
        options.setUploadKey(uploadKey);
        ExportImportFeedService.ImportFeed importFeed = null;

        options.setCategorySystemName(categorySystemName);


        boolean overwriteFeed= true;
        boolean overwriteTemplate = true;
        uploadProgressService.newUpload(uploadKey);
/*
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setUserAcknowledged(overwriteTemplate);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setShouldImport(overwriteTemplate);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(overwriteTemplate);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setUserAcknowledged(overwriteTemplate);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setShouldImport(overwriteTemplate);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(overwriteTemplate);

        options.findImportComponentOption(ImportComponent.FEED_DATA).setShouldImport(overwriteFeed);
        options.findImportComponentOption(ImportComponent.FEED_DATA).setOverwrite(overwriteFeed);
        options.findImportComponentOption(ImportComponent.FEED_DATA).setUserAcknowledged(overwriteFeed);

        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(true);
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(true);
        */
      //  importComponents = "";


        if(importComponents == null) {
            //shouldnt get here
            byte[] content = ImportUtil.streamToByteArray(fileInputStream);
            importFeed = exportImportFeedService.validateFeedForImport(fileMetaData.getFileName(),content, options);
            importFeed.setSuccess(false);
        }
        else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {
            }));
            byte[] content = ImportUtil.streamToByteArray(fileInputStream);
            importFeed = exportImportFeedService.importFeed(fileMetaData.getFileName(), content, options);
        }
        uploadProgressService.removeUpload(uploadKey);
        return Response.ok(importFeed).build();
    }

    @POST
    @Path(IMPORT_TEMPLATE_NEW)
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

        if(importComponents == null) {
            importTemplate = exportImportTemplateService.validateTemplateForImport(fileMetaData.getFileName(), content,options);
            importTemplate.setSuccess(false);
        }
        else {
            options.setImportComponentOptions(ObjectMapperSerializer.deserialize(importComponents, new TypeReference<Set<ImportComponentOption>>() {
            }));
            importTemplate = exportImportTemplateService.importTemplate(fileMetaData.getFileName(), content, options);
        }
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
    @ApiOperation("Gets the user-defined fields for categories and feeds.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the user-defined fields.", response = UserFieldCollection.class)
    )
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
    @ApiOperation("Sets the user-defined fields for categories and feeds.")
    @ApiResponses(
        @ApiResponse(code = 204, message = "The user-defined fields have been updated.")
    )
    public void setUserFields(@Nonnull final UserFieldCollection userFields) {
        metadataService.setUserFields(userFields);
    }
}
