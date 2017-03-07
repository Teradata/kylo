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

import com.google.common.collect.Lists;
import com.mifmif.common.regex.Generex;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.feedmgr.rest.model.EditFeedEntity;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupFailedException;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupTimeoutException;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.feed.DuplicateFeedNameException;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinitions;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedLineageStyle;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
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
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Feed Manager - Feeds", produces = "application/json")
@Path(FeedRestController.BASE)
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Feeds", description = "manages feeds"))
public class FeedRestController {

    private static final Logger log = LoggerFactory.getLogger(FeedRestController.class);
    public static final String BASE = "/v1/feedmgr/feeds";

    /**
     * Messages for the default locale
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("com.thinkbiganalytics.feedmgr.rest.controller.FeedMessages");
    private static final int MAX_LIMIT = 1000;
    private static final String NAMES = "/names";

    @Autowired
    @Qualifier("nifiRestClient")
    LegacyNifiRestClient nifiRestClient;

    @Autowired
    MetadataService metadataService;

    //Profile needs hive service
    @Autowired
    HiveService hiveService;

    @Autowired
    FeedManagerPreconditionService feedManagerPreconditionService;

    @Inject
    DatasourceService datasourceService;

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementService;

    @Inject
    RegisteredTemplateService registeredTemplateService;

    private MetadataService getMetadataService() {
        return metadataService;
    }






    /**
     * Creates a new Feed using the specified metadata.
     *
     * @param editFeedEntity the feed metadata
     * @return the feed
     */
    @POST
    @Path("/edit/{feedId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates or updates a feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed including any error messages.", response = NifiFeed.class)
    )
    @Nonnull
    public Response editFeed(@Nonnull final EditFeedEntity editFeedEntity) {
return createFeed(editFeedEntity.getFeedMetadata());

    }

    /**
     * Creates a new Feed using the specified metadata.
     *
     * @param feedMetadata the feed metadata
     * @return the feed
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates or updates a feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed including any error messages.", response = NifiFeed.class)
    )
    @Nonnull
    public Response createFeed(@Nonnull final FeedMetadata feedMetadata) {
        NifiFeed feed;
        try {
            feed = getMetadataService().createFeed(feedMetadata);
        } catch (DuplicateFeedNameException e) {
            log.info("Failed to create a new feed due to another feed having the same category/feed name: " + feedMetadata.getCategoryAndFeedDisplayName());

            // Create an error message
            String msg = "A feed already exists in the category \"" + e.getCategoryName() + "\" with name name \"" + e.getFeedName() + "\"";

            // Add error message to feed
            feed = new NifiFeed(feedMetadata, null);
            feed.addErrorMessage(msg);
            feed.setSuccess(false);
        } catch (Exception e) {
            log.error("Failed to create a new feed.", e);

            // Create an error message
            String msg = (e.getMessage() != null) ? "Error saving Feed " + e.getMessage() : "An unknown error occurred while saving the feed.";
            if (e.getCause() instanceof JDBCException) {
                msg += ". " + ((JDBCException) e).getSQLException();
            }

            // Add error message to feed
            feed = new NifiFeed(feedMetadata, null);
            feed.addErrorMessage(msg);
            feed.setSuccess(false);
        }
        return Response.ok(feed).build();
    }

    @POST
    @Path("/enable/{feedId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Enables a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The feed was enabled.", response = FeedSummary.class),
                      @ApiResponse(code = 500, message = "The feed could not be enabled.", response = RestResponseStatus.class)
                  })
    public Response enableFeed(@PathParam("feedId") String feedId) {
        FeedSummary feed = getMetadataService().enableFeed(feedId);
        return Response.ok(feed).build();
    }

    @POST
    @Path("/disable/{feedId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Disables a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The feed was disabled.", response = FeedSummary.class),
                      @ApiResponse(code = 500, message = "The feed could not be disabled.", response = RestResponseStatus.class)
                  })
    public Response disableFeed(@PathParam("feedId") String feedId) {
        FeedSummary feed = getMetadataService().disableFeed(feedId);
        return Response.ok(feed).build();
    }

    @GET
    @Path(NAMES)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of feed summaries.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns a list of feeds.", response = FeedSummary.class, responseContainer = "List")
    )
    public Response getFeedNames() {
        Collection<FeedSummary> feeds = getMetadataService().getFeedSummaryData();
        return Response.ok(feeds).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of feeds.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns a list of feeds.", response = FeedMetadata.class, responseContainer = "List")
    )
    public Response getFeeds(@QueryParam("verbose") @DefaultValue("false") boolean verbose) {
        Collection<? extends UIFeed> feeds = getMetadataService().getFeeds(verbose);
        return Response.ok(feeds).build();
    }

    @GET
    @Path("/{feedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed.", response = FeedMetadata.class),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFeed(@PathParam("feedId") String feedId) {
        FeedMetadata feed = getMetadataService().getFeedById(feedId, true);

        return Response.ok(feed).build();
    }

    /**
     * Deletes the specified feed.
     *
     * @param feedId the feed id
     * @return the response
     */
    @DELETE
    @Path("/{feedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The feed was deleted."),
                      @ApiResponse(code = 404, message = "The feed does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 409, message = "There are dependent feeds.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be deleted.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response deleteFeed(@Nonnull @PathParam("feedId") final String feedId) {
        try {
            getMetadataService().deleteFeed(feedId);
            return Response.noContent().build();
        } catch (AccessControlException e) {
            log.debug("Access controll failure attempting to delete a feed", e);
            throw e;
        } catch (FeedCleanupFailedException e) {
            log.error("Error deleting feed: Cleanup error", e);
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.cleanupError"), e);
        } catch (FeedCleanupTimeoutException e) {
            log.error("Error deleting feed: Cleanup timeout", e);
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.cleanupTimeout"), e);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting feed: Illegal Argument", e);
            throw new NotFoundException(STRINGS.getString("deleteFeed.notFound"), e);
        } catch (final IllegalStateException e) {
            log.error("Error deleting feed: Illegal state", e);
            throw new ClientErrorException(STRINGS.getString("deleteFeed.hasDependents"), Response.Status.CONFLICT, e);
        } catch (NifiClientRuntimeException e) {
            log.error("Error deleting feed: NiFi error", e);
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.nifiError"), e);
        } catch (Exception e) {
            log.error("Error deleting feed: Unknown error", e);
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.unknownError"), e);
        }
    }

    @POST
    @Path("/{feedId}/merge-template")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates a feed with the latest template metadata.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The feed was updated.", response = FeedMetadata.class),
                      @ApiResponse(code = 500, message = "The feed could not be updated.", response = RestResponseStatus.class)
                  })
    public Response mergeTemplate(@PathParam("feedId") String feedId, FeedMetadata feed) {
        registeredTemplateService.mergeTemplatePropertiesWithFeed(feed);
        return Response.ok(feed).build();
    }




    @GET
    @Path("/{feedId}/profile-summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a summary of the feed profiles.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the profile summaries.", response = Map.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "The profiles are unavailable.", response = RestResponseStatus.class)
                  })
    public Response profileSummary(@PathParam("feedId") String feedId) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        final String profileTable = HiveUtils.quoteIdentifier(feedMetadata.getProfileTableName());
        String query = "SELECT * from " + profileTable + " where columnname = '(ALL)'";

        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            QueryResult results = hiveService.query(query);

            rows.addAll(results.getRows());
            //add in the archive date time fields if applicipable
            String ARCHIVE_PROCESSOR_TYPE = "com.thinkbiganalytics.nifi.GetTableData";
            if (feedMetadata.getInputProcessorType().equalsIgnoreCase(ARCHIVE_PROCESSOR_TYPE)) {
                NifiProperty property = NifiPropertyUtil.findPropertyByProcessorType(feedMetadata.getProperties(), ARCHIVE_PROCESSOR_TYPE, "Date Field");
                if (property != null && property.getValue() != null) {
                    String field = property.getValue();
                    if (field.contains(".")) {
                        field = StringUtils.substringAfterLast(field, ".");
                    }
                    query = "SELECT * from " + profileTable + " where metrictype IN('MIN_TIMESTAMP','MAX_TIMESTAMP') AND columnname = " + HiveUtils.quoteString(field);

                    QueryResult dateRows = hiveService.query(query);
                    if (dateRows != null && !dateRows.isEmpty()) {
                        rows.addAll(dateRows.getRows());
                    }
                }
            }
        } catch (DataAccessException e) {
            if (e.getCause() instanceof org.apache.hive.service.cli.HiveSQLException && e.getCause().getMessage().contains("Table not found")) {
                //this exception is ok to swallow since it just means no profile data exists yet
            } else if (e.getCause().getMessage().contains("HiveAccessControlException Permission denied")) {
                throw new AccessControlException("You do not have permission to execute this hive query");
            } else {
                throw e;
            }
        }

        return Response.ok(rows).build();
    }

    @GET
    @Path("/{feedId}/profile-stats")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the profile statistics for the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the profile statistics.", response = Map.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "The profile is unavailable.", response = RestResponseStatus.class)
                  })
    public Response profileStats(@PathParam("feedId") String feedId, @QueryParam("processingdttm") String processingdttm) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String profileTable = feedMetadata.getProfileTableName();
        String query = "SELECT * from " + HiveUtils.quoteIdentifier(profileTable) + " where processing_dttm = " + HiveUtils.quoteString(processingdttm);
        QueryResult rows = hiveService.query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-invalid-results")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets statistics on the invalid rows for the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the invalid row statistics.", response = Map.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "The profile is unavailable.", response = RestResponseStatus.class)
                  })
    public Response queryProfileInvalidResults(
        @PathParam("feedId") String feedId,
        @QueryParam("processingdttm") String processingdttm,
        @QueryParam("limit") int limit,
        @QueryParam("filter") String filter) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String condition = "";
        if (StringUtils.isNotBlank(filter)) {
            condition = " and dlp_reject_reason like " + HiveUtils.quoteString("%" + filter + "%") + " ";
        }
        return getPage(processingdttm, limit, feedMetadata.getInvalidTableName(), condition);
    }

    @GET
    @Path("/{feedId}/profile-valid-results")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets statistics on the valid rows for the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the valid row statistics.", response = Map.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "The profile is unavailable.", response = RestResponseStatus.class)
                  })
    public Response queryProfileValidResults(
        @PathParam("feedId") String feedId,
        @QueryParam("processingdttm") String processingdttm,
        @QueryParam("limit") int limit) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        return getPage(processingdttm, limit, feedMetadata.getValidTableName());
    }

    private Response getPage(String processingdttm, int limit, String table) {
        return getPage(processingdttm, limit, table, null);
    }

    private Response getPage(String processingdttm, int limit, String table, String filter) {
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        } else if (limit < 1) {
            limit = 1;
        }
        StringBuilder query = new StringBuilder("SELECT * from " + HiveUtils.quoteIdentifier(table) + " where processing_dttm = " + HiveUtils.quoteString(processingdttm) + " ");
        if (StringUtils.isNotBlank(filter)) {
            query.append(filter);
        }
        query.append(" limit ").append(limit);
        QueryResult rows = hiveService.query(query.toString());
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/possible-preconditions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the available preconditions for triggering a feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the available precondition rules.", response = PreconditionRule.class, responseContainer = "List")
    )
    public Response getPossiblePreconditions() {
        List<PreconditionRule> conditions = feedManagerPreconditionService.getPossiblePreconditions();
        return Response.ok(conditions).build();
    }

    @GET
    @Path("/{feedId}/sla")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the service level agreements referenced by a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the service level agreements.", response = FeedServiceLevelAgreement.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getSla(@PathParam("feedId") String feedId) {
        List<FeedServiceLevelAgreement> sla = serviceLevelAgreementService.getFeedServiceLevelAgreements(feedId);
        return Response.ok(sla).build();
    }

    @POST
    @Path("/update-feed-lineage-styles")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the feed lineage styles.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The styles were updated.", response = RestResponseStatus.class)
    )
    public Response updateFeedLineageStyles(Map<String, FeedLineageStyle> styles) {
        datasourceService.refreshFeedLineageStyles(styles);
        return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildSuccess()).build();
    }

    @POST
    @Path("/update-datasource-definitions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the datasource definitions.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the updated definitions..", response = DatasourceDefinitions.class)
    )
    public DatasourceDefinitions updateDatasourceDefinitions(DatasourceDefinitions definitions) {
        if (definitions != null) {
            Set<DatasourceDefinition> updatedDefinitions = datasourceService.updateDatasourceDefinitions(definitions.getDefinitions());
            if (updatedDefinitions != null) {
                return new DatasourceDefinitions(Lists.newArrayList(updatedDefinitions));
            }
        }
        return new DatasourceDefinitions();
    }

    @POST
    @Path("/{feedId}/upload-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Uploads a file to be ingested by a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The file is ready to be ingested."),
                      @ApiResponse(code = 500, message = "The file could not be saved.", response = RestResponseStatus.class)
                  })
    public Response uploadFile(@PathParam("feedId") String feedId,
                               @FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {

        FeedMetadata feed = getMetadataService().getFeedById(feedId, false);
        // Derive path and file
        List<NifiProperty> properties = feed.getProperties();
        String dropzone = null;
        String regexFileFilter = null;

        for (NifiProperty property : properties) {

            if (property.getProcessorType().equals("org.apache.nifi.processors.standard.GetFile")) {
                if (property.getKey().equals("File Filter")) {
                    regexFileFilter = property.getValue();
                } else if (property.getKey().equals("Input Directory")) {
                    dropzone = property.getValue();
                }
            }
        }
        if (StringUtils.isEmpty(regexFileFilter) || StringUtils.isEmpty(dropzone)) {
            throw new IOException("Unable to upload file with empty dropzone and file");
        }
        File tempTarget = File.createTempFile("kylo-upload", "");
        String fileName = "";
        try {
            Generex fileNameGenerator = new Generex(regexFileFilter);
            fileName = fileNameGenerator.random();

            // Cleanup oddball characters generated by generex
            fileName = fileName.replaceAll("[^A-Za-z0-9\\.\\_\\+\\%\\-\\|]+", "\\.");
            java.nio.file.Path dropZoneTarget = Paths.get(dropzone, fileName);
            File dropZoneFile = dropZoneTarget.toFile();
            if (dropZoneFile.exists()) {
                throw new IOException("File with the name [" + fileName + "] already exists in [" + dropzone + "]");
            }

            Files.copy(fileInputStream, tempTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempTarget.toPath(), dropZoneTarget);

            // Set read, write
            dropZoneFile.setReadable(true);
            dropZoneFile.setWritable(true);

        } catch (AccessDeniedException e) {
            String errTemplate = "Permission denied attempting to write file [%s] to [%s]. Check with system administrator to ensure this application has write permissions to folder";
            String err = String.format(errTemplate, fileName, dropzone);
            log.error(err);
            throw new InternalServerErrorException(err);

        } catch (Exception e) {
            String errTemplate = "Unexpected exception writing file [%s] to [%s].";
            String err = String.format(errTemplate, fileName, dropzone);
            log.error(err);
            throw new InternalServerErrorException(err);
        }
        return Response.ok("").build();
    }
}

