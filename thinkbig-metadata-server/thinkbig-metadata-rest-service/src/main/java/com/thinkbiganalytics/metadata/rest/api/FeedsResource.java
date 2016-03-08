/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
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
            
            ensureDependentDatasources(feed, domainFeed);
            ensurePrecondition(feed, domainFeed);
            
            return Model.DOMAIN_TO_FEED.apply(this.feedProvider.getFeed(domainFeed.getId()));
        } else {
            throw new WebApplicationException("A feed with the given name already exists: " + feed.getSystemName(), Status.BAD_REQUEST);
        }
    }

    private void ensurePrecondition(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
        FeedPrecondition precond = feed.getPrecondition();
        
        if (precond != null) {
            Set<com.thinkbiganalytics.metadata.sla.api.Metric> domainMetrics 
            = new HashSet<>(Collections2.transform(precond.getMetrics(), Model.METRIC_TO_DOMAIN));
            
            this.feedProvider.ensurePrecondition(domainFeed.getId(), "", "", domainMetrics);
        }
        
    }

    private void ensureDependentDatasources(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
        for (FeedSource src : feed.getSources()) {
            Dataset.ID dsId = this.datasetProvider.resolve(src.getId());
            this.feedProvider.ensureFeedSource(domainFeed.getId(), dsId);
        }
        
        for (FeedDestination src : feed.getDestinations()) {
            Dataset.ID dsId = this.datasetProvider.resolve(src.getId());
            this.feedProvider.ensureFeedDestination(domainFeed.getId(), dsId);
        }
    }
    
    @POST
    @Path("{feedId}/source")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedSource(@PathParam("feedId") String feedId, 
                              @FormParam("datasourceId") String datasourceId) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = this.feedProvider.resolveFeed(feedId);
        Dataset.ID domainDsId = this.datasetProvider.resolve(datasourceId);
        
        com.thinkbiganalytics.metadata.api.feed.FeedSource domainDest 
            = this.feedProvider.ensureFeedSource(domainFeedId, domainDsId);
        
        return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
    }
    
    @POST
    @Path("{feedId}/destination")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedDestination(@PathParam("feedId") String feedId, 
                                   @FormParam("datasourceId") String datasourceId) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = this.feedProvider.resolveFeed(feedId);
        Dataset.ID domainDsId = this.datasetProvider.resolve(datasourceId);
        
        com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest 
            = this.feedProvider.ensureFeedDestination(domainFeedId, domainDsId);
        
        return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
    }
}
