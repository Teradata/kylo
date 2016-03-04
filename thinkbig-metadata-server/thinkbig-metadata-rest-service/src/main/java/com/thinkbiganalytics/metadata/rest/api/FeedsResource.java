/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/feed")
public class FeedsResource {
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DatasetProvider datasetProvider;
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Feed> getFeeds() {
        Collection<com.thinkbiganalytics.metadata.api.feed.Feed> domainFeeds = this.feedProvider.getFeeds();
        
        return new ArrayList<>(Collections2.transform(domainFeeds, Model.DOMAIN_TO_FEED));
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Feed createFeed(Feed feed) {
        Model.validateCreate(feed);
        
        FeedCriteria crit = this.feedProvider.feedCriteria().name(feed.getSystemName());
        Collection<com.thinkbiganalytics.metadata.api.feed.Feed> existing = this.feedProvider.getFeeds(crit);
        
        if (existing.isEmpty()) {
            com.thinkbiganalytics.metadata.api.feed.Feed domainFeed = this.feedProvider.ensureFeed(feed.getSystemName(), feed.getDescription());
            
            for (FeedSource src : feed.getSources()) {
                Dataset.ID dsId = this.datasetProvider.resolve(src.getId());
                this.feedProvider.ensureFeedSource(domainFeed.getId(), dsId);
            }
            
            for (FeedDestination src : feed.getDestinations()) {
                Dataset.ID dsId = this.datasetProvider.resolve(src.getId());
                this.feedProvider.ensureFeedDestination(domainFeed.getId(), dsId);
            }
            
            return Model.DOMAIN_TO_FEED.apply(this.feedProvider.getFeed(domainFeed.getId()));
        } else {
            throw new WebApplicationException("A feed with the given name already exists: " + feed.getSystemName(), Status.BAD_REQUEST);
        }
    }
    
    @POST
    @Path("{feedId}/destination")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedDestinationForm(@PathParam("feedId") String feedId, 
                                       @FormParam("datasourceId") String datasourceId,  // TODO always null
                                       @QueryParam("dsId") String dsIdParam) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = this.feedProvider.resolveFeed(feedId);
        Dataset.ID domainDsId = this.datasetProvider.resolve(dsIdParam);
//        Dataset.ID domainDsId = this.datasetProvider.resolve(datasourceId);
        
        com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest 
            = this.feedProvider.ensureFeedDestination(domainFeedId, domainDsId);
        
        return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
    }
    
    @POST
    @Path("{fid}/source")
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedSource(@PathParam("fid") String feedId, @QueryParam("dsid") String datasourceId) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = this.feedProvider.resolveFeed(feedId);
        Dataset.ID domainDsId = this.datasetProvider.resolve(datasourceId);
        com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc 
            = this.feedProvider.ensureFeedSource(domainFeedId, domainDsId);
        
        return Model.DOMAIN_TO_FEED.apply(domainSrc.getFeed());
    }
}
