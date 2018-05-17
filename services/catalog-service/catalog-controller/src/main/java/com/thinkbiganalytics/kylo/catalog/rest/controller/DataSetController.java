package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.kylo.catalog.DataSetNotFound;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path(DataSetController.BASE)
public class DataSetController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSetController.class);

    public static final String BASE = "/v1/catalog/dataset";

    private static final MessageSource MESSAGES;

    static {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("DataSetMessages");
        MESSAGES = messageSource;
    }

    @Inject
    CatalogFileManager fileManager;

    @Inject
    HttpServletRequest request;

    @GET
    @Path("{id}/browse")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists files on path")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of files on path", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Datasource does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list files", response = RestResponseStatus.class)
                  })
    public Response listFiles(@PathParam("id") @UUID final String dataSourceId, @QueryParam("path") String path) {
        log.entry(dataSourceId);

        final List<DataSetFile> files;
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            files = new ArrayList<>(0);
        } else {
            files = new ArrayList<>(list.length);
            for (File file : list) {
                DataSetFile dataSetFile = new DataSetFile();
                dataSetFile.setDirectory(file.isDirectory());
                dataSetFile.setLength(file.length());
                dataSetFile.setModificationTime(file.lastModified());
                dataSetFile.setName(file.getName());
                dataSetFile.setPath(file.getPath());
                files.add(dataSetFile);
            }
        }

        return log.exit(Response.ok(files).build());
    }

    @GET
    @Path("{id}/uploads")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists uploaded files for a data set.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of uploaded files", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list uploads", response = RestResponseStatus.class)
                  })
    public Response getUploads(@PathParam("id") @UUID final String dataSetId) {
        log.entry(dataSetId);

        final List<DataSetFile> files;
        try {
            log.debug("Listing uploaded files for dataset {}", dataSetId);
            files = fileManager.listUploads(dataSetId);
        } catch (final DataSetNotFound | IllegalArgumentException e) {
            log.debug("Unable to retrieve dataset uploads: {}", e, e);
            throw new NotFoundException(getMessage("getUploads.notFound"));
        } catch (final IOException e) {
            log.error("Unable to retrieve dataset uploads: {}", e, e);
            throw new InternalServerErrorException(getMessage("getUploads.error"));
        }

        return log.exit(Response.ok(files).build());
    }

    @POST
    @Path("{id}/uploads")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Uploads a file for the data set.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Uploaded file info", response = DataSetFile.class),
                      @ApiResponse(code = 400, message = "Invalid filename", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 409, message = "A file already exists with the same name", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to upload files", response = RestResponseStatus.class)
                  })
    public Response postUpload(@PathParam("id") @UUID final String dataSetId, @Nonnull final FormDataMultiPart form) {
        log.entry(dataSetId, form);

        final List<BodyPart> bodyParts = form.getBodyParts();
        if (bodyParts.size() != 1) {
            log.debug("Wrong number of form parts for uploading to dataset {}: {}", dataSetId, bodyParts.size());
            throw new BadRequestException(getMessage("postUpload.missingBodyPart"));
        }

        final DataSetFile file;
        try {
            final BodyPart part = bodyParts.get(0);
            log.debug("Upload file [{}] for dataset {}", part.getContentDisposition().getFileName(), dataSetId);
            file = fileManager.createUpload(dataSetId, part.getContentDisposition().getFileName(), part.getEntityAs(BodyPartEntity.class).getInputStream());
        } catch (final DataSetNotFound e) {
            log.debug("Failed to save file for dataset {}: {}", dataSetId, e, e);
            throw new NotFoundException(getMessage("postUpload.notFound"));
        } catch (final FileAlreadyExistsException e) {
            log.debug("Filename conflict for uploaded file [{}] for dataset {}: {}", bodyParts.get(0).getContentDisposition().getFileName(), dataSetId, e, e);
            throw new WebApplicationException(getMessage("postUpload.fileExists"), Response.Status.CONFLICT);
        } catch (final IllegalArgumentException e) {
            log.debug("Invalid filename [{}] for uploaded file for dataset {}: {}", bodyParts.get(0).getContentDisposition().getFileName(), dataSetId, e, e);
            throw new BadRequestException(getMessage("postUpload.invalidFileName"));
        } catch (final IOException e) {
            log.error("Failed to save file for dataset {}: {}", dataSetId, e, e);
            throw new InternalServerErrorException(getMessage("postUpload.error"));
        }

        return log.exit(Response.ok(file).build());
    }

    @DELETE
    @Path("{id}/uploads/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes an upload file from a data set.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The file was deleted successfully"),
                      @ApiResponse(code = 400, message = "Invalid filename", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to delete file", response = RestResponseStatus.class)
                  })
    public Response deleteUpload(@PathParam("id") @UUID final String dataSetId, @PathParam("name") final String fileName) {
        log.entry(dataSetId, fileName);

        try {
            log.debug("Deleting uploaded file [{}] from dataset {}", fileName, dataSetId);
            fileManager.deleteUpload(dataSetId, fileName);
        } catch (final DataSetNotFound e) {
            log.debug("Failed do delete file [{}] from dataset {}: {}", fileName, dataSetId, e, e);
            throw new NotFoundException(getMessage("postUpload.notFound"));
        } catch (final IllegalArgumentException e) {
            log.debug("Invalid name for deleting file [{}] from dataset {}: {}", fileName, dataSetId, e, e);
            throw new NotFoundException(getMessage("postUpload.invalidFileName"));
        } catch (final IOException e) {
            log.error("Failed do delete file [{}] from dataset {}: {}", fileName, dataSetId, e, e);
            throw new InternalServerErrorException(getMessage("deleteUpload.error"));
        }

        return log.exit(Response.noContent().build());
    }

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return MESSAGES.getMessage(code, null, RequestContextUtils.getLocale(request));
    }
}
