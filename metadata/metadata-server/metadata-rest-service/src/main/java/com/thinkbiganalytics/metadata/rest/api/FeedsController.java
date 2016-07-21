/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation.State;
import com.thinkbiganalytics.metadata.api.op.FeedOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDependencyGraph;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.op.FeedOperation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;

/**
 *
 * @author Sean Felten
 */
@Component
@Api(value = "metadata-feeds", produces = "application/json", description = "Manage Feed Metadata and allow feeds to be updated with various Metadata Properties")
@Path("/feed")
public class FeedsController {
    
    private static final Logger LOG = LoggerFactory.getLogger(FeedsController.class);
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private FeedOperationsProvider feedOpsProvider;
    
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
        
        return this.metadata.read(() -> {
                com.thinkbiganalytics.metadata.api.feed.FeedCriteria criteria = createFeedCriteria(name, srcId, destId);
                Collection<com.thinkbiganalytics.metadata.api.feed.Feed> domainFeeds = feedProvider.getFeeds(criteria);

                return new ArrayList<>(Collections2.transform(domainFeeds, Model.DOMAIN_TO_FEED));
        });
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Feed getFeed(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {}", feedId);

        return this.metadata.read(() -> {
                com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
                com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
                
                return Model.DOMAIN_TO_FEED.apply(domain);
        });
    }
    
    @GET
    @Path("{id}/op")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedOperation> getFeedOperations(@PathParam("id") final String feedId,
                                                 @QueryParam("since") @DefaultValue("1970-01-01T00:00:00Z") String sinceStr,
                                                 @QueryParam("limit") @DefaultValue("-1") int limit) {
        final DateTime since = Formatters.parseDateTime(sinceStr);
      
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            FeedOperationCriteria criteria = feedOpsProvider.criteria()
                            .feed(domainId)
                            .stoppedSince(since)
                            .state(State.SUCCESS);
            List<com.thinkbiganalytics.metadata.api.op.FeedOperation> list = feedOpsProvider.find(criteria);
            
            return list.stream().map(op -> Model.DOMAIN_TO_FEED_OP.apply(op)).collect(Collectors.toList());
        });
    }
    
    @GET
    @Path("{id}/op/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<DateTime, Map<String, Object>> collectFeedOperationsResults(@PathParam("id") final String feedId,
                                                                           @QueryParam("since") @DefaultValue("1970-01-01T00:00:00Z") String sinceStr) {
        
        final DateTime since = Formatters.TIME_FORMATTER.parseDateTime(sinceStr);
        
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            FeedOperationCriteria criteria = feedOpsProvider.criteria()
                            .feed(domainId)
                            .stoppedSince(since)
                            .state(State.SUCCESS);
            Map<DateTime, Map<String, Object>> results = feedOpsProvider.getAllResults(criteria, null);
            
            return results.entrySet().stream()
                            .collect(Collectors.toMap(te -> te.getKey(),
                                                      te -> (Map<String, Object>) te.getValue().entrySet().stream()
                                                                      .collect(Collectors.toMap(ve -> ve.getKey(),
                                                                                                ve -> (Object) ve.getValue().toString()))));
        });
    }
    
    @GET
    @Path("{id}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedDependencyGraph getDependencyGraph(@PathParam("id") final String feedId,
                                             @QueryParam("preconds") @DefaultValue("true") final boolean assessPrecond) {
        LOG.debug("Get feed dependencies {}", feedId);

        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> startDomain = feedProvider.getFeed(domainId);
            
            if (startDomain != null) {
                return collectFeedDependencies(startDomain, assessPrecond);
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }
    
    @POST
    @Path("{feedId}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedDependencyGraph addDependent(@PathParam("feedId") final String feedIdStr, 
                                       @QueryParam("dependentId") final String depIdStr) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);
        com.thinkbiganalytics.metadata.api.feed.Feed.ID depId = this.feedProvider.resolveFeed(depIdStr);
        
        this.metadata.commit(() -> {
            this.feedProvider.addDependent(feedId, depId);
            return null;
        });
        
        return getDependencyGraph(feedId.toString(), false);
    }
    
    @DELETE
    @Path("{feedId}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedDependencyGraph removeDependent(@PathParam("feedId") final String feedIdStr, 
                                          @QueryParam("dependentId") final String depIdStr) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);
        com.thinkbiganalytics.metadata.api.feed.Feed.ID depId = this.feedProvider.resolveFeed(depIdStr);
        
        this.metadata.commit(() -> {
            this.feedProvider.removeDependent(feedId, depId);
            return null;
        });
        
        return getDependencyGraph(feedId.toString(), false);
    }
    
    @GET
    @Path("{feedId}/depfeeds/delta")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<DateTime, Map<String, Object>> getDependentResultDeltas(@PathParam("feedId") final String feedIdStr) {
        
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);
        
        return this.metadata.commit(() -> {
            Map<DateTime, Map<String, Object>> results = this.feedOpsProvider.getDependentDeltaResults(feedId, null);
            return results.entrySet().stream()
                            .collect(Collectors.toMap(te -> te.getKey(),
                                                      te -> (Map<String, Object>) te.getValue().entrySet().stream()
                                                                      .collect(Collectors.toMap(ve -> ve.getKey(),
                                                                                                ve -> (Object) ve.getValue().toString()))));
        });
    }

    @GET
    @Path("{id}/source")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedSource> getFeedSources(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} sources", feedId);

        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                return new ArrayList<>(Collections2.transform(domain.getSources(), Model.DOMAIN_TO_FEED_SOURCE));
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }
//    
//    @GET
//    @Path("{fid}/source/{sid}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public FeedSource getFeedSource(@PathParam("fid") final String feedId, @PathParam("sid") final String srcId) {
//        LOG.debug("Get feed {} source {}", feedId, srcId);
//        
//        return this.metadata.read(() -> {
//            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
//            com.thinkbiganalytics.metadata.api.feed.FeedSource.ID domainSrcId = feedProvider.resolveSource(srcId);
//            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
//            
//            if (domain != null) {
//                com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc = domain.getSource(domainSrcId);
//                
//                if (domainSrc != null) {
//                    return Model.DOMAIN_TO_FEED_SOURCE.apply(domainSrc);
//                } else {
//                    throw new WebApplicationException("A feed source with the given ID does not exist: " + srcId, Status.NOT_FOUND);
//                }
//            } else {
//                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
//            }
//        });
//    }
    
    @GET
    @Path("{id}/destination")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedDestination> getFeedDestinations(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} destinations", feedId);

        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                return new ArrayList<>(Collections2.transform(domain.getDestinations(), Model.DOMAIN_TO_FEED_DESTINATION));
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }
//    
//    @GET
//    @Path("{fid}/destination/{sid}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public FeedDestination getFeedDestination(@PathParam("fid") final String feedId, @PathParam("sid") final String destId) {
//        LOG.debug("Get feed {} destination {}", feedId, destId);
//
//        return this.metadata.read(() -> {
//            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
//            com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID domainDestId = feedProvider.resolveDestination(destId);
//            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
//            
//            if (domain != null) {
//                com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest = domain.getDestination(domainDestId);
//                
//                if (domainDest != null) {
//                    return Model.DOMAIN_TO_FEED_DESTINATION.apply(domainDest);
//                } else {
//                    throw new WebApplicationException("A feed destination with the given ID does not exist: " + destId, Status.NOT_FOUND);
//                }
//            } else {
//                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
//            }
//        });
//    }
    
    @GET
    @Path("{id}/precondition")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedPrecondition getFeedPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} precondition", feedId);

        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                return Model.DOMAIN_TO_FEED_PRECOND.apply(domain.getPrecondition());
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }
    
    @GET
    @Path("{id}/precondition/assessment")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceLevelAssessment assessPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Assess feed {} precondition", feedId);
        
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
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
        });
    }
    
    @GET
    @Path("{id}/precondition/assessment/result")
    @Produces(MediaType.TEXT_PLAIN)
    public String assessPreconditionResult(@PathParam("id") final String feedId) {
        return assessPrecondition(feedId).getResult().toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed createFeed(final Feed feed, @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
        LOG.debug("Create feed (ensure={}) {}", ensure, feed);
        
        Model.validateCreate(feed);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.FeedCriteria crit = feedProvider.feedCriteria().name(feed.getSystemName()).category(feed.getCategory().getSystemName());
            Collection<com.thinkbiganalytics.metadata.api.feed.Feed> existing = feedProvider.getFeeds(crit);
            
            if (existing.isEmpty()) {
                com.thinkbiganalytics.metadata.api.feed.Feed<?> domainFeed = feedProvider.ensureFeed(feed.getCategory().getSystemName(), feed.getSystemName(), feed.getDescription());
                
                ensureDependentDatasources(feed, domainFeed);
                ensurePrecondition(feed, domainFeed);
                ensureProperties(feed, domainFeed);
                
                return Model.DOMAIN_TO_FEED.apply(feedProvider.getFeed(domainFeed.getId()));
            } else if (ensure) {
                return Model.DOMAIN_TO_FEED.apply(existing.iterator().next());
            } else {
                throw new WebApplicationException("A feed with the given name already exists: " + feed.getSystemName(), Status.BAD_REQUEST);
            }
        });
    }
    
    /**
     * Updates an existing feed.  Note that POST is used here rather than PUT since it behaves more 
     * like a PATCH; which isn't supported in Jersey.
     */
    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Feed updateFeed(@PathParam("id") final String feedId, 
                           final Feed feed) {
        LOG.debug("Update feed: {}", feed);
        
        Model.validateCreate(feed);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                domain = Model.updateDomain(feed, domain);
                return Model.DOMAIN_TO_FEED.apply(domain);
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feed.getId(), Status.NOT_FOUND);
            }
        });
    }
    
    @GET
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getFeedProperties(@PathParam("id") final String feedId) {
        LOG.debug("Get feed properties ID: {}", feedId);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                Map<String, Object> domainProps = domain.getProperties();
                Properties newProps = new Properties();
                
                newProps.putAll(domainProps);
                return newProps;
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feedId, Status.NOT_FOUND);
            }
        });
    }
    
    @POST
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties mergeFeedProperties(@PathParam("id") final String feedId, 
                                          final Properties props) {
        LOG.debug("Merge feed properties ID: {}, properties: {}", feedId, props);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                Map<String, Object> domainProps = updateProperties(props, domain, false);
                Properties newProps = new Properties();
                
                newProps.putAll(domainProps);
                return newProps;
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feedId, Status.NOT_FOUND);
            }
        });
    }
    
    @PUT
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties replaceFeedProperties(@PathParam("id") final String feedId, 
                                            final Properties props) {
        LOG.debug("Replace feed properties ID: {}, properties: {}", feedId, props);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domain = feedProvider.getFeed(domainId);
            
            if (domain != null) {
                Map<String, Object> domainProps = updateProperties(props, domain, true);
                Properties newProps = new Properties();
                
                newProps.putAll(domainProps);
                return newProps;
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feedId, Status.NOT_FOUND);
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
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
            com.thinkbiganalytics.metadata.api.feed.FeedSource domainDest = feedProvider.ensureFeedSource(domainFeedId, domainDsId);
            
            return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
        });
    }
    
    @POST
    @Path("{feedId}/destination")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed addFeedDestination(@PathParam("feedId") final String feedId, 
                                   @FormParam("datasourceId") final String datasourceId) {
        LOG.debug("Add feed destination, feed ID: {}, datasource ID: {}", feedId, datasourceId);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
            com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest = feedProvider.ensureFeedDestination(domainFeedId, domainDsId);
            
            return Model.DOMAIN_TO_FEED.apply(domainDest.getFeed());
        });
    }
    
    @POST
    @Path("{feedId}/precondition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Feed setPrecondition(@PathParam("feedId") final String feedId, final FeedPrecondition precond) {
        LOG.debug("Add feed precondition, feed ID: {}, precondition: {}", feedId, precond);
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            List<com.thinkbiganalytics.metadata.sla.api.Metric> domainMetrics
                = precond.getSla().getObligations().stream()
                    .flatMap((grp) -> grp.getMetrics().stream())
                .map((metric) -> metric)
                    .collect(Collectors.toList());
        
            com.thinkbiganalytics.metadata.api.feed.Feed<?> domainFeed 
                = feedProvider.createPrecondition(domainFeedId, "", domainMetrics);

            return Model.DOMAIN_TO_FEED.apply(domainFeed);
        });
    }
    

    private Map<String, Object> updateProperties(final Properties props,
                                                 com.thinkbiganalytics.metadata.api.feed.Feed<?> domain,
                                                 boolean replace) {
        Map<String, Object> domainProps = domain.getProperties();
        
        if (replace) {
            domainProps.clear();
        }
        
        for (String name : props.stringPropertyNames()) {
            domainProps.put(name, props.getProperty(name));
        }
        
        return domainProps;
    }

    private ServiceLevelAssessment generateModelAssessment(com.thinkbiganalytics.metadata.api.feed.FeedPrecondition precond) {
        com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assmt = this.preconditionService.assess(precond);
        return Model.DOMAIN_TO_SLA_ASSMT.apply(assmt);
    }

    private void ensurePrecondition(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed<?> domainFeed) {
        FeedPrecondition precond = feed.getPrecondition();
        
        if (precond != null) {
            List<com.thinkbiganalytics.metadata.sla.api.Metric> domainMetrics
                = precond.getSla().getObligations().stream()
                    .flatMap((grp) -> grp.getMetrics().stream())
                .map((metric) -> metric)
                    .collect(Collectors.toList());
    
            feedProvider.createPrecondition(domainFeed.getId(), "", domainMetrics);
        }
        
    }

    private void ensureDependentDatasources(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed<?> domainFeed) {
        for (FeedSource src : feed.getSources()) {
            Datasource.ID dsId = this.datasetProvider.resolve(src.getId());
            feedProvider.ensureFeedSource(domainFeed.getId(), dsId);
        }
        
        for (FeedDestination src : feed.getDestinations()) {
            Datasource.ID dsId = this.datasetProvider.resolve(src.getId());
            feedProvider.ensureFeedDestination(domainFeed.getId(), dsId);
        }
    }
    
    private void ensureProperties(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed<?> domainFeed) {
        Map<String, Object> domainProps = domainFeed.getProperties();
        Properties props = feed.getProperties();
        
        for (String key : feed.getProperties().stringPropertyNames()) {
            domainProps.put(key, props.getProperty(key));
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

    private FeedDependencyGraph collectFeedDependencies(com.thinkbiganalytics.metadata.api.feed.Feed currentFeed, boolean assessPrecond) {
        List<com.thinkbiganalytics.metadata.api.feed.Feed<?>> domainDeps = currentFeed.getDependentFeeds();
        FeedDependencyGraph feedDep = new FeedDependencyGraph(Model.DOMAIN_TO_FEED.apply(currentFeed), null);
    
        if (! domainDeps.isEmpty()) {
            for (com.thinkbiganalytics.metadata.api.feed.Feed<?> depFeed : domainDeps) {
                FeedDependencyGraph childDep = collectFeedDependencies(depFeed, assessPrecond);
                feedDep.addDependecy(childDep);
            }
        }
        
        if (assessPrecond && currentFeed.getPrecondition() != null) {
            feedDep.setPreconditonResult(generateModelAssessment(currentFeed.getPrecondition()));
        }
        
        return feedDep;
    }

}
