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
import com.thinkbiganalytics.feedmgr.rest.model.ImportFeedOptions;
import com.thinkbiganalytics.feedmgr.rest.model.ImportProperty;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgress;
import com.thinkbiganalytics.feedmgr.rest.model.UserFieldCollection;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.feed.exporting.FeedExporter;
import com.thinkbiganalytics.feedmgr.service.feed.exporting.model.ExportFeed;
import com.thinkbiganalytics.feedmgr.service.feed.importing.FeedImporter;
import com.thinkbiganalytics.feedmgr.service.feed.importing.FeedImporterFactory;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.exporting.TemplateExporter;
import com.thinkbiganalytics.feedmgr.service.template.exporting.model.ExportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporter;
import com.thinkbiganalytics.feedmgr.service.template.importing.TemplateImporterFactory;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.util.ImportUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    TemplateExporter templateExporter;

    @Inject
    FeedExporter feedExporter;

    @Inject
    TemplateImporterFactory templateImporterFactory;

    @Inject
    FeedImporterFactory feedImporterFactory;


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
        ExportTemplate zipFile = templateExporter.exportTemplate(templateId);
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
                      @ApiResponse(code = 404, message = "The feed is not available.", response = RestResponseStatus.class)
                  })
    public Response exportFeed(@NotNull @Size(min = 36, max = 36, message = "Invalid feedId size")
                               @PathParam("feedId") String feedId) {
        try {
            ExportFeed zipFile = feedExporter.exportFeed(feedId);
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
        if (uploadProgress != null) {
            uploadProgress.checkAndIncrementPercentage();
            return Response.ok(uploadProgress).build();
        } else {
            return Response.ok(uploadProgress).build();
        }
    }


    /**
     * This is used for quick import via a script.  The UI uses the AdminControllerV2 class
     */
    @POST
    @Path(IMPORT_FEED)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Imports a feed zip file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed metadata.", response = ImportFeed.class),
                      @ApiResponse(code = 500, message = "There was a problem importing the feed.", response = RestResponseStatus.class)
                  })
    public Response uploadFeed(@NotNull @FormDataParam("file") InputStream fileInputStream,
                               @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                               @FormDataParam("overwriteFeedTemplate") @DefaultValue("false") boolean overwriteFeedTemplate,
                               @FormDataParam("categorySystemName") String categorySystemName,
                               @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportTemplateOptions.IMPORT_CONNECTING_FLOW importConnectingFlow,
                               @FormDataParam("templateProperties") @DefaultValue("") String templateProperties,
                               @FormDataParam("feedProperties") @DefaultValue("") String feedProperties)
        throws Exception {
        ImportFeedOptions options = new ImportFeedOptions();
        String uploadKey = uploadProgressService.newUpload();
        options.setUploadKey(uploadKey);
        options.findImportComponentOption(ImportComponent.FEED_DATA).setOverwrite(overwrite);
        options.findImportComponentOption(ImportComponent.FEED_DATA).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.FEED_DATA).setShouldImport(true);

        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setUserAcknowledged(!importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.NOT_SET));
        options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES) && overwriteFeedTemplate);

        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(overwriteFeedTemplate);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setShouldImport(true);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setContinueIfExists(!overwriteFeedTemplate);

        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(overwriteFeedTemplate);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setShouldImport(true);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setContinueIfExists(!overwriteFeedTemplate);

        options.setCategorySystemName(categorySystemName);

        if (StringUtils.isNotBlank(templateProperties)) {
            List<ImportProperty> properties = ObjectMapperSerializer.deserialize(templateProperties, new TypeReference<List<ImportProperty>>() {
            });
            options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setProperties(properties);
        }

        if (StringUtils.isNotBlank(feedProperties)) {
            List<ImportProperty> properties = ObjectMapperSerializer.deserialize(feedProperties, new TypeReference<List<ImportProperty>>() {
            });
            options.findImportComponentOption(ImportComponent.FEED_DATA).setProperties(properties);
        }

        byte[] content = ImportUtil.streamToByteArray(fileInputStream);
        FeedImporter feedImporter = feedImporterFactory.apply(fileMetaData.getFileName(), content, options);
        ImportFeed importFeed = feedImporter.validateAndImport();

        return Response.ok(importFeed).build();
    }


    /**
     * This is used for quick import via scripts.  The UI uses the AdminControllerV2 class
     */
    @POST
    @Path(IMPORT_TEMPLATE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Imports a template xml or zip file.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the template metadata.", response = ImportTemplate.class),
                      @ApiResponse(code = 500, message = "There was a problem importing the template.", response = RestResponseStatus.class)
                  })
    public Response uploadTemplatex(@NotNull @FormDataParam("file") InputStream fileInputStream,
                                    @NotNull @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                    @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite,
                                    @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportTemplateOptions.IMPORT_CONNECTING_FLOW importConnectingFlow,
                                    @FormDataParam("createReusableFlow") @DefaultValue("false") boolean createReusableFlow,
                                    @FormDataParam("templateProperties") @DefaultValue("") String templateProperties)
        throws Exception {

        ImportTemplateOptions options = new ImportTemplateOptions();
        String uploadKey = uploadProgressService.newUpload();
        options.setUploadKey(uploadKey);

        boolean isZip = fileMetaData.getFileName().endsWith("zip");

        if (isZip) {
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setUserAcknowledged(!importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.NOT_SET));
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(importConnectingFlow.equals(ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES));

        } else {
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setShouldImport(createReusableFlow);
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setUserAcknowledged(true);
            options.findImportComponentOption(ImportComponent.REUSABLE_TEMPLATE).setOverwrite(createReusableFlow);
        }

        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setOverwrite(overwrite);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setShouldImport(true);
        options.findImportComponentOption(ImportComponent.NIFI_TEMPLATE).setContinueIfExists(!overwrite);

        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setOverwrite(overwrite);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setUserAcknowledged(true);
        options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setShouldImport(true);
        if (StringUtils.isNotBlank(templateProperties)) {
            List<ImportProperty> properties = ObjectMapperSerializer.deserialize(templateProperties, new TypeReference<List<ImportProperty>>() {
            });
            options.findImportComponentOption(ImportComponent.TEMPLATE_DATA).setProperties(properties);
        }

        byte[] content = ImportUtil.streamToByteArray(fileInputStream);
        TemplateImporter templateImporter = templateImporterFactory.apply(fileMetaData.getFileName(), content, options);
        ImportTemplate importTemplate = templateImporter.validateAndImport();

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
