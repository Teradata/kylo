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

import com.thinkbiganalytics.kylo.catalog.dataset.DataSetProvider;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path(DataSetController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class DataSetController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSetController.class);

    public static final String BASE = "/v1/catalog/dataset";

    @Inject
    DataSetProvider dataSetProvider;

    @Inject
    CatalogFileManager fileManager;

    @Autowired
    @Qualifier("catalogMessages")
    MessageSource messages;

    @Inject
    HttpServletRequest request;

    @POST
    @ApiOperation("Creates a new data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Data set created", response = DataSet.class),
                      @ApiResponse(code = 400, message = "Invalid data source", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDataSet(@Nonnull final DataSet source) {
        log.entry(source);

        final DataSet dataSet;
        try {
            dataSet = dataSetProvider.createDataSet(source);
        } catch (final IllegalArgumentException e) {
            log.debug("Invalid data source for creating data set: {}", source, e);
            throw log.throwing(new BadRequestException(getMessage("catalog.dataset.createDataSet.invalidDataSource")));
        }

        return Response.ok(log.exit(dataSet)).build();
    }

    @GET
    @Path("{id}")
    @ApiOperation("Retrieves the specified data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data set", response = DataSet.class),
                      @ApiResponse(code = 404, message = "Data set not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response getDataSet(@PathParam("id") @UUID final String dataSetId) {
        log.entry(dataSetId);
        final DataSet dataSet = findDataSet(dataSetId);
        return Response.ok(log.exit(dataSet)).build();
    }

    @POST
    @Path("{id}/files")
    @ApiOperation("List files of a data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of files in path", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "A path is not valid", response = RestResponseStatus.class),
                      @ApiResponse(code = 403, message = "Access to the path is restricted", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list files", response = RestResponseStatus.class)
                  })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listFiles(@PathParam("id") @UUID final String dataSetId, @Nullable final DataSetTemplate changes) {
        log.entry(dataSetId, changes);

        // Apply changes to data set
        final DataSet dataSet = findDataSet(dataSetId);
        if (changes != null) {
            DataSetUtil.mergeTemplates(dataSet, changes);
        }

        // List files at path
        final List<DataSetFile> files = new ArrayList<>();

        for (final String path : dataSet.getPaths()) {
            try {
                log.debug("Listing files at path: {}", path);
                files.addAll(fileManager.listFiles(new URI(path), dataSet));
            } catch (final AccessDeniedException e) {
                throw new ForbiddenException(getMessage("catalog.dataset.listFiles.forbidden", path));
            } catch (final URISyntaxException e) {
                throw new BadRequestException(getMessage("catalog.dataset.listFiles.invalidPath", path));
            } catch (final Exception e) {
                log.error("Failed to list dataset files at path {}: {}", path, e, e);
                final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                    .message(getMessage("catalog.dataset.listFiles.error", path))
                    .url(request.getRequestURI())
                    .setDeveloperMessage(e)
                    .buildError();
                throw new InternalServerErrorException(Response.serverError().entity(status).build());
            }
        }

        return Response.ok(log.exit(files)).build();
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

        final DataSet dataSet = findDataSet(dataSetId);
        final List<DataSetFile> files;
        try {
            log.debug("Listing uploaded files for dataset {}", dataSetId);
            files = fileManager.listUploads(dataSet);
        } catch (final IllegalArgumentException e) {
            log.debug("Unable to retrieve dataset uploads: {}", e, e);
            throw new NotFoundException(getMessage("catalog.dataset.notFound"));
        } catch (final Exception e) {
            log.error("Unable to retrieve dataset uploads: {}", e, e);
            throw new InternalServerErrorException(getMessage("catalog.dataset.getUploads.error"));
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
            throw new BadRequestException(getMessage("catalog.dataset.postUpload.missingBodyPart"));
        }

        final DataSet dataSet = findDataSet(dataSetId);
        final DataSetFile file;
        try {
            final BodyPart part = bodyParts.get(0);
            log.debug("Upload file [{}] for dataset {}", part.getContentDisposition().getFileName(), dataSetId);
            file = fileManager.createUpload(dataSet, part.getContentDisposition().getFileName(), part.getEntityAs(BodyPartEntity.class).getInputStream());
        } catch (final FileAlreadyExistsException e) {
            log.debug("Filename conflict for uploaded file [{}] for dataset {}: {}", bodyParts.get(0).getContentDisposition().getFileName(), dataSetId, e, e);
            throw new WebApplicationException(getMessage("catalog.dataset.postUpload.fileExists"), Response.Status.CONFLICT);
        } catch (final IllegalArgumentException e) {
            log.debug("Invalid filename [{}] for uploaded file for dataset {}: {}", bodyParts.get(0).getContentDisposition().getFileName(), dataSetId, e, e);
            throw new BadRequestException(getMessage("catalog.dataset.invalidFileName"));
        } catch (final Exception e) {
            log.error("Failed to save file for dataset {}: {}", dataSetId, e, e);
            throw new InternalServerErrorException(getMessage("catalog.dataset.postUpload.error"));
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

        final DataSet dataSet = findDataSet(dataSetId);
        try {
            log.debug("Deleting uploaded file [{}] from dataset {}", fileName, dataSetId);
            fileManager.deleteUpload(dataSet, fileName);
        } catch (final IllegalArgumentException e) {
            log.debug("Invalid name for deleting file [{}] from dataset {}: {}", fileName, dataSetId, e, e);
            throw new NotFoundException(getMessage("catalog.dataset.invalidFileName"));
        } catch (final Exception e) {
            log.error("Failed do delete file [{}] from dataset {}: {}", fileName, dataSetId, e, e);
            throw new InternalServerErrorException(getMessage("catalog.dataset.deleteUpload.error"));
        }

        return log.exit(Response.noContent().build());
    }

    /**
     * Gets the dataset with the specified id and applies the specified changes.
     *
     * @throws NotFoundException if the dataset does not exist
     */
    @Nonnull
    private DataSet findDataSet(@Nonnull final String id) {
        return dataSetProvider.findDataSet(id).orElseThrow(() -> new NotFoundException(getMessage("catalog.dataset.notFound")));
    }

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return getMessage(code, (Object[]) null);
    }

    /**
     * Gets the specified message in the current locale with the specified arguments.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code, @Nullable final Object... args) {
        return messages.getMessage(code, args, RequestContextUtils.getLocale(request));
    }
}
