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

import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(DataSetController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class DataSetController extends AbstractCatalogController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSetController.class);

    public static final String BASE = "/v1/catalog/dataset";

    @Inject
    com.thinkbiganalytics.kylo.catalog.dataset.DataSetProvider dataSetService;

    @Inject
    CatalogFileManager fileManager;

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

        final boolean encryptCredentials = true;
        DataSet dataSet;
        try {
            dataSet = dataSetService.findOrCreateDataSet(source, encryptCredentials);
        } catch (final CatalogException e) {
            log.debug("Cannot create data set from request: {}", source, e);
            throw new BadRequestException(getMessage(e));
        }

        return Response.ok(log.exit(dataSet)).build();
    }

    @PUT
    @ApiOperation("Updates an existing data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Data set updated", response = DataSet.class),
                      @ApiResponse(code = 400, message = "Invalid data source", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDataSet(@Nonnull final DataSet source) {
        log.entry(source);

        final boolean encryptCredentials = true;
        final DataSet dataSet;
        try {
            dataSet = dataSetService.updateDataSet(source, encryptCredentials);
        } catch (final CatalogException e) {
            throw new BadRequestException(getMessage(e));
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

        final boolean encryptCredentials = true;
        final DataSet dataSet = findDataSet(dataSetId, encryptCredentials);
        return Response.ok(log.exit(dataSet)).build();
    }

    @GET
    @Path("{id}/uploads")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists uploaded files for a data set.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of uploaded files", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list uploaded files", response = RestResponseStatus.class)
                  })
    public Response getUploads(@PathParam("id") @UUID final String dataSetId) {
        log.entry(dataSetId);

        final DataSet dataSet = findDataSet(dataSetId, true);
        final List<DataSetFile> files;
        try {
            log.debug("Listing uploaded files for dataset {}", dataSetId);
            files = fileManager.listUploads(dataSet);
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
                      @ApiResponse(code = 500, message = "Failed to upload file", response = RestResponseStatus.class)
                  })
    public Response postUpload(@PathParam("id") @UUID final String dataSetId, @Nonnull final FormDataMultiPart form) {
        log.entry(dataSetId, form);

        final List<BodyPart> bodyParts = form.getBodyParts();
        if (bodyParts.size() != 1) {
            log.debug("Wrong number of form parts for uploading to dataset {}: {}", dataSetId, bodyParts.size());
            throw new BadRequestException(getMessage("catalog.dataset.postUpload.missingBodyPart"));
        }

        final DataSet dataSet = findDataSet(dataSetId, true);
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
                      @ApiResponse(code = 404, message = "Data set or file does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to delete file", response = RestResponseStatus.class)
                  })
    public Response deleteUpload(@PathParam("id") @UUID final String dataSetId, @PathParam("name") final String fileName) {
        log.entry(dataSetId, fileName);

        final DataSet dataSet = findDataSet(dataSetId, true);
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
     * Gets the dataset with the specified id.
     *
     * @throws NotFoundException if the dataset does not exist
     */
    @Nonnull
    private DataSet findDataSet(@Nonnull final String id, final boolean encryptedCredentials) {
        return dataSetService.findDataSet(id, encryptedCredentials)
            .orElseThrow(() -> {
                log.debug("Data set not found: {}", id);
                return new NotFoundException(getMessage("catalog.dataset.notFound"));
            });
    }
}
