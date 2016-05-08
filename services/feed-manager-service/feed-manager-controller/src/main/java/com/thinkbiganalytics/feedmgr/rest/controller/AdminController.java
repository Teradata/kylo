package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.support.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.schema.TextFileParser;
import io.swagger.annotations.Api;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by sr186054 on 5/6/16.
 */
@Api(value = "admin", produces = "application/json")
@Path("/v1/feedmgr/admin")
public class AdminController {

    @Inject
    ExportImportTemplateService exportImportTemplateService;

    @GET
    @Path("/export-template/{templateId}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response exportTemplate(@PathParam("templateId") String templateId) {

        ExportImportTemplateService.ExportTemplate zipFile =exportImportTemplateService.exportTemplate(templateId);
        return Response.ok(zipFile.getFile(), MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachments; filename=\"" + zipFile.getFileName() + "\"") //optional
                .build();
    }

    @POST
    @Path("/import-template")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON })
    public Response uploadPdfFile(  @FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileMetaData,
    @FormDataParam("overwrite") @DefaultValue("false") boolean overwrite) throws Exception
    {


        ExportImportTemplateService.ImportTemplate importTemplate = exportImportTemplateService.importTemplate(fileMetaData.getFileName(),fileInputStream,overwrite);
        return Response.ok(importTemplate).build();
    }









}
