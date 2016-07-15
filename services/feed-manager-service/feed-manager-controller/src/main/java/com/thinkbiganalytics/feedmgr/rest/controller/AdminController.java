package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.rest.model.ImportOptions;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;

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

/**
 * Created by sr186054 on 5/6/16.
 */
@Api(value = "admin", produces = "application/json")
@Path("/v1/feedmgr/admin")
public class AdminController {

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @Inject
    ExportImportFeedService exportImportFeedService;

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
                               @FormDataParam("importConnectingReusableFlow") @DefaultValue("NOT_SET") ImportOptions.IMPORT_CONNECTING_FLOW importConnectingFlow)
        throws Exception {
        ImportOptions options = new ImportOptions();
        options.setOverwrite(overwrite);
        options.setImportConnectingFlow(importConnectingFlow);
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


}
