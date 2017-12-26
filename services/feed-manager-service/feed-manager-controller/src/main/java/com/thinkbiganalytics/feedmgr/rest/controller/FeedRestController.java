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
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.annotations.AnnotationFieldNameResolver;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.EditFeedEntity;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupFailedException;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupTimeoutException;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.feed.DuplicateFeedNameException;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.metadata.FeedPropertySection;
import com.thinkbiganalytics.metadata.FeedPropertyType;
import com.thinkbiganalytics.metadata.api.security.MetadataAccessControl;
import com.thinkbiganalytics.metadata.modeshape.versioning.VersionNotFoundException;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinitions;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedLineageStyle;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.directory.api.util.Strings;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
    private static final String SUMMARY = "/feed-summary";

    @Inject
    private MetadataService metadataService;

    @Inject
    private HiveService hiveService;

    @Inject
    private FeedManagerPreconditionService feedManagerPreconditionService;

    @Inject
    private FeedModelTransform feedModelTransform;

    @Inject
    private DatasourceService datasourceService;

    @Inject
    private SecurityService securityService;

    @Inject
    private SecurityModelTransform securityTransform;

    @Inject
    private ServiceLevelAgreementService serviceLevelAgreementService;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    private AccessController accessController;

    @Inject
    PropertyExpressionResolver propertyExpressionResolver;

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

    private void populateFeed(EditFeedEntity editFeedEntity) {
        //fetch the feed
        FeedMetadata feed = getMetadataService().getFeedById(editFeedEntity.getFeedMetadata().getFeedId());
        FeedMetadata editFeed = editFeedEntity.getFeedMetadata();
        switch (editFeedEntity.getAction()) {
            case SUMMARY:
                updateFeedMetadata(feed, editFeed, FeedPropertySection.SUMMARY);
                break;
            case NIFI_PROPERTIES:
                updateFeedMetadata(feed, editFeed, FeedPropertySection.NIFI_PROPERTIES);
                break;
            case PROPERTIES:
                updateFeedMetadata(feed, editFeed, FeedPropertySection.PROPERTIES);
                break;
            case TABLE_DATA:
                updateFeedMetadata(feed, editFeed, FeedPropertySection.TABLE_DATA);
                break;
            case SCHEDULE:
                updateFeedMetadata(feed, editFeed, FeedPropertySection.SCHEDULE);
                break;
            default:
                break;
        }

        createFeed(feed);
    }

    private void updateFeedMetadata(FeedMetadata targetFeedMetadata, FeedMetadata modifiedFeedMetadata, FeedPropertySection feedPropertySection) {

        AnnotationFieldNameResolver annotationFieldNameResolver = new AnnotationFieldNameResolver(FeedPropertyType.class);
        List<AnnotatedFieldProperty> list = annotationFieldNameResolver.getProperties(FeedMetadata.class);
        List<AnnotatedFieldProperty>
            sectionList =
            list.stream().filter(annotatedFieldProperty -> feedPropertySection.equals(((FeedPropertyType) annotatedFieldProperty.getAnnotation()).section())).collect(Collectors.toList());
        sectionList.forEach(annotatedFieldProperty -> {
            try {
                Object value = FieldUtils.readField(annotatedFieldProperty.getField(), modifiedFeedMetadata);
                FieldUtils.writeField(annotatedFieldProperty.getField(), targetFeedMetadata, value);
            } catch (IllegalAccessException e) {
                log.warn("Unable to update FeedMetadata field: {}.  Exception: {} ", annotatedFieldProperty.getField(), e.getMessage(), e);
            }
        });


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
    @Deprecated
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
    @Path(SUMMARY)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of feed summaries.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns a list of feeds.", response = FeedSummary.class, responseContainer = "List")
    )
    public Response getFeedSummaries() {
        Collection<FeedSummary> feeds = getMetadataService().getFeedSummaryData();
        return Response.ok(feeds).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of feeds.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns a list of feeds.", response = FeedMetadata.class, responseContainer = "List")
    )
    public SearchResult getFeeds(@QueryParam("verbose") @DefaultValue("false") boolean verbose,
                                 @QueryParam("sort") @DefaultValue("feedName") String sort,
                                 @QueryParam("filter") String filter,
                                 @QueryParam("limit") String limit,
                                 @QueryParam("start") @DefaultValue("0") Integer start) {

        try {
            int size = Strings.isEmpty(limit) || limit.equalsIgnoreCase("all") ? MAX_LIMIT : Integer.parseInt(limit);
            Page<UIFeed> page = getMetadataService().getFeedsPage(verbose,
                                                                  pageRequest(start, size, sort),
                                                                  filter != null ? filter.trim() : null);
            return this.feedModelTransform.toSearchResult(page);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value of limit must be an integer or \"all\"");
        }
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

    @GET
    @Path("/by-name/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed.", response = FeedMetadata.class),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFeedByName(@PathParam("feedName") String feedName) {
        String categorySystemName = FeedNameUtil.category(feedName);
        String feedSystemName = FeedNameUtil.feed(feedName);
        if (StringUtils.isNotBlank(categorySystemName) && StringUtils.isNotBlank(feedSystemName)) {
            FeedMetadata feed = getMetadataService().getFeedByName(categorySystemName, feedSystemName);
            return Response.ok(feed).build();
        } else {
            throw new NotFoundException("Unable to find the feed for name: " + feedName);
        }
    }

    @GET
    @Path("/by-name/{feedName}/field-policies")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed field policies (List<FieldPolicy>) as json.", response = List.class),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFeedFieldPoliciesByName(@PathParam("feedName") String feedName) {
        String categorySystemName = FeedNameUtil.category(feedName);
        String feedSystemName = FeedNameUtil.feed(feedName);
        if (StringUtils.isNotBlank(categorySystemName) && StringUtils.isNotBlank(feedSystemName)) {
            FeedMetadata feed = getMetadataService().getFeedByName(categorySystemName, feedSystemName);
            if (feed != null && feed.getTable() != null) {
                return Response.ok(feed.getTable().getFieldPoliciesJson()).build();
            } else {
                throw new NotFoundException("Unable to find the feed field policies for name: " + feedName);
            }
        } else {
            throw new NotFoundException("Unable to find the feed field policies for name: " + feedName);
        }
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

    @GET
    @Path("/{feedId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates a feed with the latest template metadata.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed versions.", response = FeedMetadata.class),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFeedVersions(@PathParam("feedId") String feedId,
                                    @QueryParam("content") @DefaultValue("true") boolean includeContent) {
        FeedVersions feed = getMetadataService().getFeedVersions(feedId, includeContent);

        return Response.ok(feed).build();
    }

    @GET
    @Path("/{feedId}/versions/{versionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates a feed with the latest template metadata.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed versions.", response = FeedMetadata.class),
                      @ApiResponse(code = 400, message = "Returns the feed or version does not exist.", response = FeedMetadata.class),
                      @ApiResponse(code = 500, message = "The feed is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getFeedVersion(@PathParam("feedId") String feedId,
                                   @PathParam("versionId") String versionId,
                                   @QueryParam("content") @DefaultValue("true") boolean includeContent) {
        try {
            return getMetadataService().getFeedVersion(feedId, versionId, includeContent)
                .map(version -> Response.ok(version).build())
                .orElse(Response.status(Status.NOT_FOUND).build());
        } catch (VersionNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected exception retrieving the feed version", e);
            throw new InternalServerErrorException("Unexpected exception retrieving the feed version");
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

    @GET
    @Path("{feedId}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the feed's roles")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A feed with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getRoleMemberships(@PathParam("feedId") String feedIdStr,
                                       @QueryParam("verbose") @DefaultValue("false") boolean verbose) {
        // TODO: No longer using verbose; all results are verbose now.
        return this.securityService.getFeedRoleMemberships(feedIdStr)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }


    @POST
    @Path("{feedId}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the members of one of a feed's roles.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "No feed exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("feedId") String feedIdStr,
                                          RoleMembershipChange changes) {
        return this.securityService.changeFeedRoleMemberships(feedIdStr, changes)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("Either a feed with the ID \"" + feedIdStr
                                                           + "\" does not exist or it does not have a role the named \""
                                                           + changes.getRoleName() + "\"", Status.NOT_FOUND));
    }

    @GET
    @Path("{feedId}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A feed with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAvailableActions(@PathParam("feedId") String feedIdStr) {
        log.debug("Get available actions for feed: {}", feedIdStr);

        return this.securityService.getAvailableFeedActions(feedIdStr)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @GET
    @Path("{feedId}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A feed with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAllowedActions(@PathParam("feedId") String feedIdStr,
                                      @QueryParam("user") Set<String> userNames,
                                      @QueryParam("group") Set<String> groupNames) {
        log.debug("Get allowed actions for feed: {}", feedIdStr);

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedFeedActions(feedIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @POST
    @Path("{feedId}/actions/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the permissions for a feed using the supplied permission change request.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No feed exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("feedId") String feedIdStr,
                                          PermissionsChange changes) {

        return this.securityService.changeFeedPermissions(feedIdStr, changes)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @GET
    @Path("{feedId}/actions/change")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Constructs and returns a permission change request for a set of users/groups containing the actions that the requester may permit or revoke.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the change request that may be modified by the client and re-posted.", response = PermissionsChange.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No feed exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public PermissionsChange getAllowedPermissionsChange(@PathParam("feedId") String feedIdStr,
                                                         @QueryParam("type") String changeType,
                                                         @QueryParam("user") Set<String> userNames,
                                                         @QueryParam("group") Set<String> groupNames) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Status.BAD_REQUEST);
        }

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.createFeedPermissionChange(feedIdStr,
                                                               ChangeType.valueOf(changeType.toUpperCase()),
                                                               Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
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
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
        List<FeedServiceLevelAgreement> sla = serviceLevelAgreementService.getFeedServiceLevelAgreements(feedId);
        if (sla == null) {
            throw new WebApplicationException("No SLAs for the feed were found", Response.Status.NOT_FOUND);
        }
        return Response.ok(sla).build();
    }

    @POST
    @Path("/update-all-datasources")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        "Updates ALL  sources/destinations used for the feed lineage for ALL feeds.  WARNING: This will be an expensive call if you have lots of feeds.  This will remove all existing sources/destinations and revaluate the feed and its template for sources/destinations")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "All the feed datasources were updated", response = RestResponseStatus.class),
                  })
    public Response updateAllFeedDataSources() {
        this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
        getMetadataService().updateAllFeedsDatasources();
        return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildSuccess()).build();
    }


    @POST
    @Path("/{feedId}/update-datasources")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates a feeds sources/destinations used for the FeedLineage.  This will remove all existing sources/destinations and revaluate the feed and its template for sources/destinations")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "the datasources were updated", response = RestResponseStatus.class),
                  })
    public Response updateFeedDatasources(@PathParam("feedId") String feedId) {
        this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
        getMetadataService().updateFeedDatasources(feedId);
        return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildSuccess()).build();
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
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

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
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ADMIN_FEEDS);

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
        feed = registeredTemplateService.mergeTemplatePropertiesWithFeed(feed);
        propertyExpressionResolver.resolvePropertyExpressions(feed);
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
            throw new InternalServerErrorException(err, e);
        }
        return Response.ok("").build();
    }

    private PageRequest pageRequest(Integer start, Integer limit, String sort) {
        if (StringUtils.isNotBlank(sort)) {
            Sort.Direction dir = Sort.Direction.ASC;
            if (sort.startsWith("-")) {
                dir = Sort.Direction.DESC;
                sort = sort.substring(1);
            }
            return new PageRequest((start / limit), limit, dir, sort);
        } else {
            return new PageRequest((start / limit), limit);
        }
    }
}

