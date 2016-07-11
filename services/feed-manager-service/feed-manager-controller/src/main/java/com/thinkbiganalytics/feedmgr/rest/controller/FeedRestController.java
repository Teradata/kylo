package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.schema.TextFileParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.JDBCException;
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
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-feeds", produces = "application/json")
@Path("/v1/feedmgr/feeds")
@Component
public class FeedRestController {

    @Autowired
    @Qualifier("nifiRestClient")
    NifiRestClient nifiRestClient;


    @Autowired
    MetadataService metadataService;

    //Profile needs hive service

    @Autowired
    HiveService hiveService;

    @Autowired
    FeedManagerPreconditionService feedManagerPreconditionService;

    public FeedRestController() {
        int i = 0;
    }

    private MetadataService getMetadataService() {
        return metadataService;
    }


    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createFeed(FeedMetadata feedMetadata) {
        NifiFeed feed = null;
        try {
            feed = getMetadataService().createFeed(feedMetadata);
        } catch (Exception e) {
            //Database Exception.. post back to the user as an error
            feed = new NifiFeed(feedMetadata, null);
            String msg = "Error saving Feed " + e.getMessage();

            if (e.getCause() instanceof JDBCException) {
                JDBCException dae = (JDBCException) e.getCause();
                msg += ".  " + dae.getSQLException();
            }
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
            InputPortsEntity inputPortsEntity = nifiRestClient.getInputPorts(metadata.getNifiProcessGroupId());
            if (inputPortsEntity != null) {
                portMap.put(metadata.getFeedName(), inputPortsEntity.getInputPorts());
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
                //CHANGED!!!!!
                feed.setTemplateId(registeredTemplate.getId());
            }
        }
        if (registeredTemplate != null) {
            NifiPropertyUtil.matchAndSetPropertyByProcessorName(registeredTemplate.getProperties(), feed.getProperties(),NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES);
            feed.setProperties(registeredTemplate.getProperties());
        }

        return Response.ok(feed).build();
    }


    @POST
    @Path("/table/sample-file")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadPdfFile(@FormDataParam("file") InputStream fileInputStream,
                                  @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {
        TextFileParser parser = new TextFileParser();
        TableSchema schema = parser.parse(fileInputStream);
        return Response.ok(schema).build();
    }


    @GET
    @Path("/{feedId}/profile-summary")
    @Produces({MediaType.APPLICATION_JSON})
    //TODO rework and move logic to proper Service/provider class
    public Response profileSummary(@PathParam("feedId") String feedId) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String profileTable = feedMetadata.getProfileTableName();
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
                    query = "SELECT * from " + profileTable + " where metrictype IN('MIN_TIMESTAMP','MAX_TIMESTAMP') AND columnname = '" + field + "'";

                    QueryResult dateRows = hiveService.query(query);
                    if (dateRows != null && !dateRows.isEmpty()) {
                        rows.addAll(dateRows.getRows());
                    }
                }
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
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
        String query = "SELECT * from " + profileTable + " where processing_dttm = '" + processingdttm + "'";
        QueryResult rows = hiveService.query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-invalid-results")
    @Produces({MediaType.APPLICATION_JSON})
    public Response queryProfileInvalidResults(@PathParam("feedId") String feedId, @QueryParam("processingdttm") String processingdttm) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String table = feedMetadata.getInvalidTableName();
        String query = "SELECT * from " + table + " where processing_dttm  = '" + processingdttm + "'";
        QueryResult rows = hiveService.query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-valid-results")
    @Produces({MediaType.APPLICATION_JSON})
    public Response queryProfileValidResults(@PathParam("feedId") String feedId, @QueryParam("processingdttm") String processingdttm) {
        FeedMetadata feedMetadata = getMetadataService().getFeedById(feedId);
        String table = feedMetadata.getValidTableName();
        String query = "SELECT * from " + table + " where processing_dttm  = '" + processingdttm + "'";
        QueryResult rows = hiveService.query(query);
        return Response.ok(rows.getRows()).build();
    }


    @GET
    @Path("/possible-preconditions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPossiblePreconditions() {
        List<GenericUIPrecondition> conditions = feedManagerPreconditionService.getPossiblePreconditions();
        return Response.ok(conditions).build();
    }
}
