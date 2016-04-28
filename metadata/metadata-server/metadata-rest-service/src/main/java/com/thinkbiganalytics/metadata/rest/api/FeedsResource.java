/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDependency;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/feed")
public class FeedsResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(FeedsResource.class);
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DatasourceProvider datasetProvider;
    
    @Inject
    private FeedPreconditionService preconditionService;
    
    @Inject
    private MetadataAccess metadata;
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Feed> getFeeds(@QueryParam(FeedCriteria.NAME) final String name,
                               @QueryParam(FeedCriteria.SRC_ID) final String srcId,
                               @QueryParam(FeedCriteria.DEST_ID) final String destId) {
        LOG.debug("Get feeds {}/{}/{}", name, srcId, destId);
        
        return this.metadata.read(new Command<List<Feed>>() {
            @Override
            public List<Feed> execute() {
                com.thinkbiganalytics.metadata.api.feed.FeedCriteria criteria = createFeedCriteria(name, srcId, destId);
                Collection<com.thinkbiganalytics.metadata.api.feed.Feed> domainFeeds = feedProvider.getFeeds(criteria);

                return new ArrayList<>(Collections2.transform(domainFeeds, Model.DOMAIN_TO_FEED));
            }
        });
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Feed getFeed(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {}", feedId);

        return this.metadata.read(new Command<Feed>() {
            @Override
            public Feed execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                return Model.DOMAIN_TO_FEED.apply(domain);
            }
        });
    }
    
    @GET
    @Path("{id}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedDependency getFeedDependency(@PathParam("id") final String feedId,
                                            @QueryParam("preconds") @DefaultValue("true") final boolean assessPrecond) {
        LOG.debug("Get feed dependencies {}", feedId);

        return this.metadata.read(new Command<FeedDependency>() {
            @Override
            public FeedDependency execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed startDomain = feedProvider.getFeed(domainId);
                
                if (startDomain != null) {
                    return collectFeedDependencies(startDomain, assessPrecond);
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }

    @GET
    @Path("{id}/source")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedSource> getFeedSources(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} sources", feedId);

        return this.metadata.read(new Command<List<FeedSource>>() {
            @Override
            public List<FeedSource> execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    return new ArrayList<>(Collections2.transform(domain.getSources(), Model.DOMAIN_TO_FEED_SOURCE));
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{fid}/source/{sid}")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedSource getFeedSource(@PathParam("fid") final String feedId, @PathParam("sid") final String srcId) {
        LOG.debug("Get feed {} source {}", feedId, srcId);
        
        return this.metadata.read(new Command<FeedSource>() {
            @Override
            public FeedSource execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.FeedSource.ID domainSrcId = feedProvider.resolveSource(srcId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc = domain.getSource(domainSrcId);
                    
                    if (domainSrc != null) {
                        return Model.DOMAIN_TO_FEED_SOURCE.apply(domainSrc);
                    } else {
                        throw new WebApplicationException("A feed source with the given ID does not exist: " + srcId, Status.NOT_FOUND);
                    }
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{id}/destination")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedDestination> getFeedDestinations(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} destinations", feedId);

        return this.metadata.read(new Command<List<FeedDestination>>() {
            @Override
            public List<FeedDestination> execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    return new ArrayList<>(Collections2.transform(domain.getDestinations(), Model.DOMAIN_TO_FEED_DESTINATION));
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{fid}/destination/{sid}")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedDestination getFeedDestination(@PathParam("fid") final String feedId, @PathParam("sid") final String destId) {
        LOG.debug("Get feed {} destination {}", feedId, destId);

        return this.metadata.read(new Command<FeedDestination>() {
            @Override
            public FeedDestination execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID domainDestId = feedProvider.resolveDestination(destId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest = domain.getDestination(domainDestId);
                    
                    if (domainDest != null) {
                        return Model.DOMAIN_TO_FEED_DESTINATION.apply(domainDest);
                    } else {
                        throw new WebApplicationException("A feed destination with the given ID does not exist: " + destId, Status.NOT_FOUND);
                    }
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{id}/precondition")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedPrecondition getFeedPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} precondition", feedId);

        return this.metadata.read(new Command<FeedPrecondition>() {
            @Override
            public FeedPrecondition execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    return Model.DOMAIN_TO_FEED_PRECOND.apply(domain.getPrecondition());
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{id}/precondition/assessment")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceLevelAssessment assessPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Assess feed {} precondition", feedId);
        
        return this.metadata.read(new Command<ServiceLevelAssessment>() {
            @Override
            public ServiceLevelAssessment execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.asFeedId(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
                
                if (domain != null) {
                    com.thinkbiganalytics.metadata.api.feed.FeedPrecondition precond = domain.getPrecondition();
                    
                    if (precond != null) {
                        return generateModelAssessment(precond);
                    } else {
                        throw new WebApplicationException("The feed with the given ID does not have a precondition: " + feedId, Status.BAD_REQUEST);
                    }
                } else {
                    throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
                }
            }
        });
    }
    
    @GET
    @Path("{id}/precondition/assessment/result")
    @Produces(MediaType.TEXT_PLAIN)
    public String assessPreconditionResult(@PathParam("id") final String feedId) {
        return assessPrecondition(feedId).getResult().toString();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Feed createFeed(final Feed feed, @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
        LOG.debug("Create feed (ensure={}) {}", ensure, feed);
        
        Model.validateCreate(feed);
        
        return this.metadata.commit(new Command<Feed>() {
            @Override
            public Feed execute() {
                com.thinkbiganalytics.metadata.api.feed.FeedCriteria crit = feedProvider.feedCriteria().name(feed.getSystemName());
                Collection<com.thinkbiganalytics.metadata.api.feed.Feed> existing = feedProvider.getFeeds(crit);
                
                if (existing.isEmpty()) {
                    com.thinkbiganalytics.metadata.api.feed.Feed domainFeed = feedProvider.ensureFeed(feed.getSystemName(), feed.getDescription());
                    
                    ensureDependentDatasources(feed, domainFeed);
                    ensurePrecondition(feed, domainFeed);
                    
                    return Model.DOMAIN_TO_FEED.apply(feedProvider.getFeed(domainFeed.getId()));
                } else if (ensure) {
                    return Model.DOMAIN_TO_FEED.apply(existing.iterator().next());
                } else {
                    throw new WebApplicationException("A feed with the given name already exists: " + feed.getSystemName(), Status.BAD_REQUEST);
                }
            }
        });
    }

    @POST
    @Path("{feedId}/source")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedSource(@PathParam("feedId") final String feedId, 
                              @FormParam("datasourceId") final String datasourceId) {
        LOG.debug("Add feed source, feed ID: {}, datasource ID: {}", feedId, datasourceId);
        
        return this.metadata.commit(new Command<Feed>() {
            @Override
            public Feed execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
                Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
                com.thinkbiganalytics.metadata.api.feed.FeedSource domainDest 
                = feedProvider.ensureFeedSource(domainFeedId, domainDsId);
                
                return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
            }
        });
    }
    
    @POST
    @Path("{feedId}/destination")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedDestination(@PathParam("feedId") final String feedId, 
                                   @FormParam("datasourceId") final String datasourceId) {
        LOG.debug("Add feed destination, feed ID: {}, datasource ID: {}", feedId, datasourceId);
        
        return this.metadata.commit(new Command<Feed>() {
            @Override
            public Feed execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
                Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
                com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest 
                    = feedProvider.ensureFeedDestination(domainFeedId, domainDsId);
                
                return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
            }
        });
    }
    
    @POST
    @Path("{feedId}/precondition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed setPrecondition(@PathParam("feedId") final String feedId, final FeedPrecondition precond) {
        LOG.debug("Add feed precondition, feed ID: {}, precondition: {}", feedId, precond);
        
        return this.metadata.commit(new Command<Feed>() {
            @Override
            public Feed execute() {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
                List<List<com.thinkbiganalytics.metadata.sla.api.Metric>> domainMetrics = new ArrayList<>();
                
                for (List<Metric> metrics : precond.getMetricGroups()) {
                    domainMetrics.add(new ArrayList<>(Collections2.transform(metrics, Model.METRIC_TO_DOMAIN)));
                }
                
                com.thinkbiganalytics.metadata.api.feed.Feed domainFeed 
                    = feedProvider.updatePrecondition(domainFeedId, domainMetrics);

                return Model.DOMAIN_TO_FEED.apply(domainFeed);
            }
        });
    }

    private ServiceLevelAssessment generateModelAssessment(com.thinkbiganalytics.metadata.api.feed.FeedPrecondition precond) {
        com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assmt = this.preconditionService.assess(precond);
        return Model.DOMAIN_TO_SLA_ASSMT.apply(assmt);
    }

    private void ensurePrecondition(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
        FeedPrecondition precond = feed.getPrecondition();
        
        if (precond != null) {
            List<List<com.thinkbiganalytics.metadata.sla.api.Metric>> domainMetrics = new ArrayList<>();
            
            for (List<Metric> metrics : precond.getMetricGroups()) {
                domainMetrics.add(new ArrayList<>(Collections2.transform(metrics, Model.METRIC_TO_DOMAIN)));
            }
            
            feedProvider.ensurePrecondition(domainFeed.getId(), "", "", domainMetrics);
        }
        
    }

    private void ensureDependentDatasources(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
        for (FeedSource src : feed.getSources()) {
            Datasource.ID dsId = this.datasetProvider.resolve(src.getId());
            feedProvider.ensureFeedSource(domainFeed.getId(), dsId);
        }
        
        for (FeedDestination src : feed.getDestinations()) {
            Datasource.ID dsId = this.datasetProvider.resolve(src.getId());
            feedProvider.ensureFeedDestination(domainFeed.getId(), dsId);
        }
    }
    
    private com.thinkbiganalytics.metadata.api.feed.FeedCriteria createFeedCriteria(String name,
                                            String srcId,
                                            String destId) {
        com.thinkbiganalytics.metadata.api.feed.FeedCriteria criteria = feedProvider.feedCriteria();
        
        if (StringUtils.isNotEmpty(name)) criteria.name(name);
        if (StringUtils.isNotEmpty(srcId)) {
            Datasource.ID dsId = this.datasetProvider.resolve(srcId);
            criteria.sourceDatasource(dsId);
        }
        if (StringUtils.isNotEmpty(destId)) {
            Datasource.ID dsId = this.datasetProvider.resolve(destId);
            criteria.destinationDatasource(dsId);
        }
        
        return criteria;
    }

    private FeedDependency collectFeedDependencies(com.thinkbiganalytics.metadata.api.feed.Feed currentFeed, boolean assessPrecond) {
        List<com.thinkbiganalytics.metadata.api.feed.FeedSource> domainSrcs = currentFeed.getSources();
        com.thinkbiganalytics.metadata.api.feed.FeedCriteria domainCrit = feedProvider.feedCriteria();
        FeedDependency feedDep = new FeedDependency(Model.DOMAIN_TO_FEED.apply(currentFeed), null, null);
    
        if (! domainSrcs.isEmpty()) {
            for (com.thinkbiganalytics.metadata.api.feed.FeedSource src : domainSrcs) {
                domainCrit.destinationDatasource(src.getDatasource().getId());
            }
            
            Collection<com.thinkbiganalytics.metadata.api.feed.Feed> domainChildFeeds = feedProvider.getFeeds(domainCrit);
            
            for (com.thinkbiganalytics.metadata.api.feed.Feed childFeed : domainChildFeeds) {
                FeedDependency childDep = collectFeedDependencies(childFeed, assessPrecond);
                feedDep.addDependecy(childDep);
            }
        }
        
        if (assessPrecond && currentFeed.getPrecondition() != null) {
            feedDep.setPreconditonResult(generateModelAssessment(currentFeed.getPrecondition()));
        }
        
        return feedDep;
    }

}
