package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.GenericUIPrecondition;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.HiveService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.PreconditionFactory;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.schema.TextFileParser;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
    @Qualifier("metadataClient")
    MetadataClient metadataClient;

    @Autowired
    MetadataService metadataService;

    @Autowired
    PreconditionFactory preconditionFactory;

    public FeedRestController() {
        int i = 0;
    }

    private MetadataService getMetadataService(){
        return metadataService;
    }




    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON })
    public Response createFeed(FeedMetadata feedMetadata) throws JerseyClientException{

        NifiFeed feed = null;
        //replace expressions with values
        feedMetadata.getTable().updateMetadataFieldValues();
        feedMetadata.getSchedule().updateDependentFeedNamesString();

            //get all the properties for the metadata
            RegisteredTemplate
                registeredTemplateProperties = getMetadataService().getRegisteredTemplateWithAllProperties(feedMetadata.getTemplateId());
            List<NifiProperty> matchedProperties =  NifiPropertyUtil.matchAndSetPropertyByIdKey(registeredTemplateProperties.getProperties(), feedMetadata.getProperties());
            feedMetadata.setProperties(registeredTemplateProperties.getProperties());
            //resolve any ${metadata.} properties
            List<NifiProperty> resolvedProperties = PropertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

            //store all input related properties as well
            List<NifiProperty> inputProperties = NifiPropertyUtil
                .findInputProperties(registeredTemplateProperties.getProperties());

            ///store only those matched and resolved in the final metadata store
            Set<NifiProperty> updatedProperties = new HashSet<>();
            updatedProperties.addAll(matchedProperties);
            updatedProperties.addAll(resolvedProperties);
            updatedProperties.addAll(inputProperties);
            feedMetadata.setProperties(new ArrayList<NifiProperty>(updatedProperties));

            NifiProcessGroup
                entity = nifiRestClient.createTemplateInstanceAsProcessGroup(registeredTemplateProperties.getTemplateId(), feedMetadata.getCategory().getSystemName(), feedMetadata.getFeedName(), feedMetadata.getInputProcessorType(), feedMetadata.getProperties(), feedMetadata.getSchedule());
            if (entity.isSuccess()) {
                feedMetadata.setNifiProcessGroup(entity);
                Date createDate = new Date();
                feedMetadata.setCreateDate(createDate);
                feedMetadata.setUpdateDate(createDate);

                getMetadataService().saveFeed(feedMetadata);
            }
            else {
                //rollback feed
            }
            feed = new NifiFeed(feedMetadata, entity);

            return Response.ok(feed).build();
    }





    @GET
    @Path("/names")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getFeedNames(){
        Collection<FeedSummary> feeds = getMetadataService().getFeedSummaryData();
        return Response.ok(feeds).build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON })
    public Response getFeeds(@QueryParam("verbose") @DefaultValue("false") boolean verbose){
        Collection<? extends UIFeed> feeds = getMetadataService().getFeeds(verbose);
        return Response.ok(feeds).build();
    }

    @GET
    @Path("/{feedId}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getFeed(@PathParam("feedId") Long feedId ) throws JerseyClientException{
        FeedMetadata feed =getMetadataService().getFeed(feedId);

        return Response.ok(feed).build();
    }

    @POST
    @Path("/{feedId}/merge-template")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON })
    public Response mergeTemplate(@PathParam("feedId") Long feedId,FeedMetadata feed ) throws JerseyClientException{
        //gets the feed data and then gets the latest template associated with that feed and merges the properties into the feed
        RegisteredTemplate registeredTemplate = null;
        try {
            registeredTemplate = getMetadataService().getRegisteredTemplateWithAllProperties(feed.getTemplateId());
        }catch (Exception e){
            registeredTemplate = getMetadataService().getRegisteredTemplateByName(feed.getTemplateName());
            if(registeredTemplate != null) {
                feed.setTemplateId(registeredTemplate.getTemplateId());
            }
        }
        if(registeredTemplate != null) {
            NifiPropertyUtil.matchAndSetPropertyByProcessorName(registeredTemplate.getProperties(), feed.getProperties());
            feed.setProperties(registeredTemplate.getProperties());
        }

        return Response.ok(feed).build();
    }


    @POST
    @Path("/table/sample-file")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON })
    public Response uploadPdfFile(  @FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
    {
        TextFileParser parser = new TextFileParser();
        TableSchema schema = parser.parse(fileInputStream);
        return Response.ok(schema).build();
    }




    @GET
    @Path("/{feedId}/profile-summary")
    @Produces({MediaType.APPLICATION_JSON })
    public Response profileSummary(@PathParam("feedId") String feedId) throws JerseyClientException {
        FeedMetadata feedMetadata = getMetadataService().getFeed(new Long(feedId));
        String profileTable = feedMetadata.getProfileTableName();
        String query = "SELECT * from "+profileTable+" where columnname = '(ALL)'";
        QueryResult results = HiveService.getInstance().getHiveRestClient().query(query);
        List<Map<String,Object>> rows = new ArrayList<>();
        rows.addAll(results.getRows());
        //add in the archive date time fields if applicipable
        String ARCHIVE_PROCESSOR_TYPE = "com.thinkbiganalytics.nifi.GetTableData";
        if(feedMetadata.getInputProcessorType().equalsIgnoreCase(ARCHIVE_PROCESSOR_TYPE)){
            NifiProperty property = NifiPropertyUtil.findPropertyByProcessorType(feedMetadata.getProperties(), ARCHIVE_PROCESSOR_TYPE, "Date Field");
            if(property != null && property.getValue() != null) {
                String field = property.getValue();
                if (field.contains(".")) {
                    field = StringUtils.substringAfterLast(field, ".");
                }
                query = "SELECT * from " + profileTable + " where metrictype IN('MIN_TIMESTAMP','MAX_TIMESTAMP') AND columnname = '" + field + "'";

               QueryResult dateRows = HiveService.getInstance().getHiveRestClient().query(query);
                if(dateRows != null && !dateRows.isEmpty()){
                    rows.addAll(dateRows.getRows());
                }
            }
        }

        return Response.ok(rows).build();
    }


    @GET
    @Path("/{feedId}/profile-stats")
    @Produces({MediaType.APPLICATION_JSON })
    public Response profileStats(@PathParam("feedId") String feedId,@QueryParam("processingdttm") String processingdttm) throws JerseyClientException {
        FeedMetadata feedMetadata = getMetadataService().getFeed(new Long(feedId));
        String profileTable = feedMetadata.getProfileTableName();
        String query = "SELECT * from "+profileTable+" where processing_dttm = '"+processingdttm+"'";
        QueryResult rows = HiveService.getInstance().getHiveRestClient().query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-invalid-results")
    @Produces({MediaType.APPLICATION_JSON })
    public Response queryProfileInvalidResults(@PathParam("feedId") String feedId,@QueryParam("processingdttm") String processingdttm) throws JerseyClientException {
        FeedMetadata feedMetadata = getMetadataService().getFeed(new Long(feedId));
       String table = feedMetadata.getInvalidTableName();
        String query = "SELECT * from "+table+" where processing_dttm  = '"+processingdttm +"'";
        QueryResult rows = HiveService.getInstance().getHiveRestClient().query(query);
        return Response.ok(rows.getRows()).build();
    }

    @GET
    @Path("/{feedId}/profile-valid-results")
    @Produces({MediaType.APPLICATION_JSON })
    public Response queryProfileValidResults(@PathParam("feedId") String feedId,@QueryParam("processingdttm") String processingdttm) throws JerseyClientException {
        FeedMetadata feedMetadata = getMetadataService().getFeed(new Long(feedId));
        String table = feedMetadata.getValidTableName();
        String query = "SELECT * from "+table+" where processing_dttm  = '"+processingdttm +"'";
        QueryResult rows = HiveService.getInstance().getHiveRestClient().query(query);
        return Response.ok(rows.getRows()).build();
    }



    @GET
    @Path("/possible-preconditions")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getPossiblePreconditions(){
        List<GenericUIPrecondition> conditions = preconditionFactory.getPossiblePreconditions();
        return Response.ok(conditions).build();
    }
}
