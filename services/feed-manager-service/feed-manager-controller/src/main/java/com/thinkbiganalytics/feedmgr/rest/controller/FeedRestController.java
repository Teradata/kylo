package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupFailedException;
import com.thinkbiganalytics.feedmgr.service.FeedCleanupTimeoutException;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.feed.DuplicateFeedNameException;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;
import com.thinkbiganalytics.schema.TextFileParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
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

@Api(value = "feed-manager-feeds", produces = "application/json")
@Path("/v1/feedmgr/feeds")
@Component
public class FeedRestController {

    private static final Logger log = LoggerFactory.getLogger(FeedRestController.class);

    /** Messages for the default locale */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("com.thinkbiganalytics.feedmgr.rest.controller.FeedMessages");
    private static final int MAX_LIMIT = 1000;

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
    ServiceLevelAgreementService serviceLevelAgreementService;

    private MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Creates a new Feed using the specified metadata.
     *
     * @param feedMetadata the feed metadata
     * @return the feed
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation("Creates a new Feed from a metadata object.")
    @ApiResponse(code = 200, message = "Returns the new feed.", response = NifiFeed.class)
    @Nonnull
    public Response createFeed(@Nonnull final FeedMetadata feedMetadata) {
        NifiFeed feed;
        try {
            feed = getMetadataService().createFeed(feedMetadata);
        } catch (DuplicateFeedNameException e) {
            log.info("Failed to create a new feed due to another feed having the same category/feed name: " + feedMetadata.getCategoryAndFeedDisplayName());
            
            // Create an error message
            String msg = "A feed already exists in the cantegory \"" + e.getCategoryName() + "\" with name name \"" + e.getFeedName() + "\"";

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
    @Produces({MediaType.APPLICATION_JSON})
    public Response enableFeed(@PathParam("feedId") String feedId) {
        FeedSummary feed = getMetadataService().enableFeed(feedId);
        return Response.ok(feed).build();
    }

    @POST
    @Path("/disable/{feedId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response disableFeed(@PathParam("feedId") String feedId) {
        FeedSummary feed = getMetadataService().disableFeed(feedId);
        return Response.ok(feed).build();
    }

    @GET
    @Path("/reusable-feeds")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getReusableFeeds() {
        List<FeedMetadata> reusableFeeds = getMetadataService().getReusableFeeds();
        return Response.ok(reusableFeeds).build();
    }


    @GET
    @Path("/reusable-feed-input-ports")
    @Produces({MediaType.APPLICATION_JSON})
    //NOT USED... Not implemented
    public Response getReusableFeedInputPorts() {
        List<FeedMetadata> reusableFeeds = getMetadataService().getReusableFeeds();
        Map<String, Set<PortDTO>> portMap = new HashMap<>();
        for (FeedMetadata metadata : reusableFeeds) {
            //fetch the ports
            Set<PortDTO> inputPortsEntity = nifiRestClient.getInputPorts(metadata.getNifiProcessGroupId());
            if (inputPortsEntity != null) {
                portMap.put(metadata.getFeedName(), inputPortsEntity);
            }
        }
        return Response.ok(portMap).build();
    }

    @GET
    @Path("/names")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFeedNames() {
        Collection<FeedSummary> feeds = getMetadataService().getFeedSummaryData();
        return Response.ok(feeds).build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFeeds(@QueryParam("verbose") @DefaultValue("false") boolean verbose) {
        Collection<? extends UIFeed> feeds = getMetadataService().getFeeds(verbose);
        return Response.ok(feeds).build();
    }

    @GET
    @Path("/{feedId}")
    @Produces({MediaType.APPLICATION_JSON})
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
    @Nonnull
    public Response deleteFeed(@Nonnull @PathParam("feedId") final String feedId) {
        try {
            getMetadataService().deleteFeed(feedId);
            return Response.noContent().build();
        } catch (FeedCleanupFailedException e) {
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.cleanupError"), e);
        } catch (FeedCleanupTimeoutException e) {
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.cleanupTimeout"), e);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(STRINGS.getString("deleteFeed.notFound"), e);
        } catch (NifiClientRuntimeException e) {
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.nifiError"), e);
        } catch (Exception e) {
            throw new InternalServerErrorException(STRINGS.getString("deleteFeed.unknownError"), e);
        }
    }

    @POST
    @Path("/{feedId}/merge-template")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response mergeTemplate(@PathParam("feedId") String feedId, FeedMetadata feed) {
        //gets the feed data and then gets the latest template associated with that feed and merges the properties into the feed
        RegisteredTemplate registeredTemplate = null;
        try {
            registeredTemplate = getMetadataService().getRegisteredTemplateWithAllProperties(feed.getTemplateId());
        } catch (Exception e) {
            registeredTemplate = getMetadataService().getRegisteredTemplateByName(feed.getTemplateName());
            if (registeredTemplate != null) {
                feed.setTemplateId(registeredTemplate.getId());
            }
        }
        if (registeredTemplate != null) {
            NifiPropertyUtil
                    .matchAndSetPropertyByProcessorName(registeredTemplate.getProperties(), feed.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
            feed.setProperties(registeredTemplate.getProperties());
        }
        registeredTemplate.initializeProcessors();
        feed.setRegisteredTemplate(registeredTemplate);

        return Response.ok(feed).build();
    }

    @POST
    @Path("/table/sample-file")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadFile(@FormDataParam("delimiter") Character delimiter,
                               @FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {
        TextFileParser parser = new TextFileParser();
        TableSchema schema = parser.parse(fileInputStream, delimiter);
        return Response.ok(schema).build();
    }

    @GET
    @Path("/{feedId}/profile-summary")
    @Produces({MediaType.APPLICATION_JSON})
    //TODO rework and move logic to proper Service/provider class
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
            } else {
                throw e;
            }
        }

        return Response.ok(rows).build();
    }

    @GET
    @Path("/{feedId}/profile-stats")
    @Produces({MediaType.APPLICATION_JSON})
    public Response profileStats(@PathParam("feedId") String feedId, @QueryParam("processingdttm") String processingdttm) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String profileTable = feedMetadata.getProfileTableName();
        String query = "SELECT * from " + HiveUtils.quoteIdentifier(profileTable) + " where processing_dttm = " + HiveUtils.quoteString(processingdttm);
        QueryResult rows = hiveService.query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-invalid-results")
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPossiblePreconditions() {
        List<PreconditionRule> conditions = feedManagerPreconditionService.getPossiblePreconditions();
        return Response.ok(conditions).build();
    }

    @GET
    @Path("/{feedId}/sla")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSla(@PathParam("feedId") String feedId) {
        List<FeedServiceLevelAgreement> sla = serviceLevelAgreementService.getFeedServiceLevelAgreements(feedId);
        return Response.ok(sla).build();
    }
}
