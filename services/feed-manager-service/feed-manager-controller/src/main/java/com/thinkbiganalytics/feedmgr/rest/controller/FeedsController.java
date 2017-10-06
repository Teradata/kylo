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

import com.thinkbiganalytics.feedmgr.rest.FeedLineageBuilder;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataModelTransform;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDependencyGraph;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedLineage;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Manages Feed Metadata and allows feeds to be updated with various Metadata Properties.
 */
@Component
@Api(tags = "Feed Manager - Feeds", produces = "application/json")
@Path("/v1/metadata/feed")
public class FeedsController {

    private static final Logger LOG = LoggerFactory.getLogger(FeedsController.class);

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private FeedOperationsProvider feedOpsProvider;

    @Inject
    private DatasourceProvider datasetProvider;

    @Inject
    private DatasourceService datasourceService;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Inject
    private FeedPreconditionService preconditionService;

    @Inject
    private SecurityService securityService;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private MetadataModelTransform metadataTransform;

    @Inject
    private SecurityModelTransform actionsTransform;

    @Inject
    private AccessController accessController;

    @Inject
    private Model model;

    @Inject
    private DatasourceModelTransform datasourceTransform;


    @GET
    @Path("{id}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A feed with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public ActionGroup getAvailableActions(@PathParam("id") String feedIdStr) {
        LOG.debug("Get available actions for feed: {}", feedIdStr);

        return this.securityService.getAvailableFeedActions(feedIdStr)
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A feed with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public ActionGroup getAllowedActions(@PathParam("id") String feedIdStr,
                                         @QueryParam("user") Set<String> userNames,
                                         @QueryParam("group") Set<String> groupNames) {
        LOG.debug("Get allowed actions for feed: {}", feedIdStr);

        Set<? extends Principal> users = Arrays.stream(this.actionsTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.actionsTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedFeedActions(feedIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @POST
    @Path("{id}/actions/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the permissions for a feed using the supplied permission change request.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No feed exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public ActionGroup postPermissionsChange(@PathParam("id") String feedIdStr,
                                             PermissionsChange changes) {

        return this.securityService.changeFeedPermissions(feedIdStr, changes)
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/change/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Constructs and returns a permission change request for a set of users/groups containing the actions that the requester may permit or revoke.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the change request that may be modified by the client and re-posted.", response = PermissionsChange.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No feed exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public PermissionsChange getAllowedPermissionsChange(@PathParam("id") String feedIdStr,
                                                         @QueryParam("type") String changeType,
                                                         @QueryParam("user") Set<String> userNames,
                                                         @QueryParam("group") Set<String> groupNames) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Status.BAD_REQUEST);
        }

        Set<? extends Principal> users = Arrays.stream(this.actionsTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.actionsTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.createFeedPermissionChange(feedIdStr,
                                                               ChangeType.valueOf(changeType.toUpperCase()),
                                                               Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .orElseThrow(() -> new WebApplicationException("A feed with the given ID does not exist: " + feedIdStr, Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/initstatus")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the registration status for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the registration status.", response = InitializationStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public InitializationStatus getInitializationStatus(@PathParam("id") String feedIdStr) {
        LOG.debug("Get feed initialization status {}", feedIdStr);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                return metadataTransform.domainToInitStatus().apply(feed.getCurrentInitStatus());
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Path("{id}/initstatus/history")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the registration history for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the registration history.", response = InitializationStatus.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public List<InitializationStatus> getInitializationStatusHistory(@PathParam("id") String feedIdStr) {
        LOG.debug("Get feed initialization history {}", feedIdStr);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                return feed.getInitHistory().stream().map(metadataTransform.domainToInitStatus()).collect(Collectors.toList());
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @PUT
    @Path("{id}/initstatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Sets the registration status for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The registration status was updated."),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The registration status could not be updated.", response = RestResponseStatus.class)
                  })
    public void putInitializationStatus(@PathParam("id") String feedIdStr,
                                        InitializationStatus status) {
        LOG.debug("Get feed initialization status {}", feedIdStr);

        this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                com.thinkbiganalytics.metadata.api.feed.InitializationStatus.State newState
                    = com.thinkbiganalytics.metadata.api.feed.InitializationStatus.State.valueOf(status.getState().name());
                feed.updateInitStatus(new com.thinkbiganalytics.metadata.api.feed.InitializationStatus(newState));
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Path("{id}/watermark")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the HighWaterMarks used by the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the HighWaterMark names.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public List<String> getHighWaterMarks(@PathParam("id") String feedIdStr) {
        LOG.debug("Get feed watermarks {}", feedIdStr);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                List<String> list = feed.getWaterMarkNames().stream().collect(Collectors.toList());
                Collections.sort(list);
                return list;
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Path("{id}/watermark/{name}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @ApiOperation("Gets the value for a specific HighWaterMark.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the HighWaterMark value.", response = String.class),
                      @ApiResponse(code = 404, message = "The HighWaterMark could not be found.", response = RestResponseStatus.class)
                  })
    public String getHighWaterMark(@PathParam("id") String feedIdStr,
                                   @PathParam("name") String waterMarkName) {
        LOG.debug("Get feed watermark {}: {}", feedIdStr, waterMarkName);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                return feed.getWaterMarkValue(waterMarkName)
                    .orElseThrow(() -> new WebApplicationException("A feed high-water mark with the given name does not exist: " + waterMarkName, Status.NOT_FOUND));
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @PUT
    @Path("{id}/watermark/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Sets the value for a specific HighWaterMark.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The HighWaterMark value has been changed."),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The HighWaterMark value could not be changed.", response = RestResponseStatus.class)
                  })
    public void putHighWaterMark(@PathParam("id") String feedIdStr,
                                 @PathParam("name") String waterMarkName,
                                 String value) {
        LOG.debug("Get feed watermark {}: {}", feedIdStr, waterMarkName);

        this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                feed.setWaterMarkValue(waterMarkName, value);
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @DELETE
    @Path("{id}/watermark/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Deletes the specified HighWaterMark.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The HighWaterMark has been deleted."),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The HighWaterMark could not be deleted.", response = RestResponseStatus.class)
                  })
    public void deleteHighWaterMark(@PathParam("id") String feedIdStr,
                                    @PathParam("name") String waterMarkName) {
        LOG.debug("Get feed watermark {}: {}", feedIdStr, waterMarkName);

        this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = feedProvider.resolveFeed(feedIdStr);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(feedId);

            if (feed != null) {
                feed.setWaterMarkValue(waterMarkName, null);
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a list of feeds.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the matching feeds.", response = Feed.class, responseContainer = "List")
    )
    public List<Feed> getFeeds(@QueryParam(FeedCriteria.CATEGORY) final String category,
                               @QueryParam(FeedCriteria.NAME) final String name,
                               @QueryParam(FeedCriteria.SRC_ID) final String srcId,
                               @QueryParam(FeedCriteria.DEST_ID) final String destId) {
        LOG.debug("Get feeds {}/{}/{}", name, srcId, destId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.FeedCriteria criteria = createFeedCriteria(category, name, srcId, destId);
            Collection<com.thinkbiganalytics.metadata.api.feed.Feed> domainFeeds = feedProvider.getFeeds(criteria);
            return domainFeeds.stream().map(metadataTransform.domainToFeed()).collect(Collectors.toList());
        });
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed.", response = Feed.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public Feed getFeed(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {}", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

            return this.metadataTransform.domainToFeed().apply(domain);
        });
    }

    /*
    @GET
    @Path("{id}/op")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedOperation> getFeedOperations(@PathParam("id") final String feedId,
                                                 @QueryParam("since") @DefaultValue("1970-01-01T00:00:00Z") String sinceStr,
                                                 @QueryParam("limit") @DefaultValue("-1") int limit) {
        final DateTime since = Formatters.parseDateTime(sinceStr);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

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
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

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
  */
    @GET
    @Path("{id}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the dependencies of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the dependency graph.", response = FeedDependencyGraph.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public FeedDependencyGraph getDependencyGraph(@PathParam("id") final String feedId,
                                                  @QueryParam("preconds") @DefaultValue("false") final boolean assessPrecond) {
        LOG.debug("Get feed dependencies {}", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed startDomain = feedProvider.getFeed(domainId);

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
    @ApiOperation("Adds a dependency to the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the new dependency graph.", response = FeedDependencyGraph.class),
                      @ApiResponse(code = 500, message = "The dependency could not be added.", response = RestResponseStatus.class)
                  })
    public FeedDependencyGraph addDependent(@PathParam("feedId") final String feedIdStr,
                                            @QueryParam("dependentId") final String depIdStr) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);
        com.thinkbiganalytics.metadata.api.feed.Feed.ID depId = this.feedProvider.resolveFeed(depIdStr);

        this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            this.feedProvider.addDependent(feedId, depId);
            return null;
        });

        return getDependencyGraph(feedId.toString(), false);
    }

    @DELETE
    @Path("{feedId}/depfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Removes a dependency from the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the new dependency graph.", response = FeedDependencyGraph.class),
                      @ApiResponse(code = 500, message = "The dependency could not be removed.", response = RestResponseStatus.class)
                  })
    public FeedDependencyGraph removeDependent(@PathParam("feedId") final String feedIdStr,
                                               @QueryParam("dependentId") final String depIdStr) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);
        com.thinkbiganalytics.metadata.api.feed.Feed.ID depId = this.feedProvider.resolveFeed(depIdStr);

        this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            this.feedProvider.removeDependent(feedId, depId);
            return null;
        });

        return getDependencyGraph(feedId.toString(), false);
    }

    @GET
    @Path("{feedId}/depfeeds/delta")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the dependencies delta for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the dependencies deltas.", response = FeedDependencyDeltaResults.class),
                      @ApiResponse(code = 500, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public FeedDependencyDeltaResults getDependentResultDeltas(@PathParam("feedId") final String feedIdStr) {
        LOG.info("Get feed dependencies  delta for {}", feedIdStr);
        com.thinkbiganalytics.metadata.api.feed.Feed.ID feedId = this.feedProvider.resolveFeed(feedIdStr);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);
            FeedDependencyDeltaResults results = this.feedOpsProvider.getDependentDeltaResults(feedId, null);
            return results;
        });
    }

    @GET
    @Path("{id}/source")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the sources of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed sources.", response = FeedSource.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public List<FeedSource> getFeedSources(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} sources", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

            if (domain != null) {
                return domain.getSources().stream().map(this.metadataTransform.domainToFeedSource()).collect(Collectors.toList());
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
//            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
//
//            if (domain != null) {
//                com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc = domain.getSource(domainSrcId);
//
//                if (domainSrc != null) {
//                    return Model.domainToFeedSource.apply(domainSrc);
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
    @ApiOperation("Gets the destinations of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed destinations.", response = FeedDestination.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public List<FeedDestination> getFeedDestinations(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} destinations", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

            if (domain != null) {
                return domain.getDestinations().stream().map(this.metadataTransform.domainToFeedDestination()).collect(Collectors.toList());
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
//            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);
//
//            if (domain != null) {
//                com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest = domain.getDestination(domainDestId);
//
//                if (domainDest != null) {
//                    return Model.domainToFeedDestination.apply(domainDest);
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
    @ApiOperation("Gets the precondition for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed precondition.", response = FeedPrecondition.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public FeedPrecondition getFeedPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Get feed {} precondition", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

            if (domain != null) {
                return this.metadataTransform.domainToFeedPrecond().apply(domain.getPrecondition());
            } else {
                throw new WebApplicationException("A feed with the given ID does not exist: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Path("{id}/precondition/assessment")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Assess the precondition of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the assessment.", response = ServiceLevelAssessment.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID or the feed does not have a precondition.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public ServiceLevelAssessment assessPrecondition(@PathParam("id") final String feedId) {
        LOG.debug("Assess feed {} precondition", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
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
        });
    }

    @GET
    @Path("{id}/precondition/assessment/result")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Assess the precondition of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the assessment result.", response = ServiceLevelAssessment.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID or the feed does not have a precondition.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class)
                  })
    public String assessPreconditionResult(@PathParam("id") final String feedId) {
        return assessPrecondition(feedId).getResult().toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates a new feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The feed was created.", response = Feed.class),
                      @ApiResponse(code = 400, message = "The name is already in use.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be created.", response = RestResponseStatus.class)
                  })
    public Feed createFeed(final Feed feed, @QueryParam("ensure") @DefaultValue("true") final boolean ensure) {
        LOG.debug("Create feed (ensure={}) {}", ensure, feed);

        this.metadataTransform.validateCreate(feed);

        return this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.FeedCriteria crit = feedProvider.feedCriteria().name(feed.getSystemName()).category(feed.getCategory().getSystemName());
            Collection<com.thinkbiganalytics.metadata.api.feed.Feed> existing = feedProvider.getFeeds(crit);

            if (existing.isEmpty()) {
                com.thinkbiganalytics.metadata.api.feed.Feed domainFeed = feedProvider.ensureFeed(feed.getCategory().getSystemName(), feed.getSystemName(), feed.getDescription());

                ensureDependentDatasources(feed, domainFeed);
                ensurePrecondition(feed, domainFeed);
                ensureProperties(feed, domainFeed);

                return this.metadataTransform.domainToFeed().apply(feedProvider.getFeed(domainFeed.getId()));
            } else if (ensure) {
                return this.metadataTransform.domainToFeed().apply(existing.iterator().next());
            } else {
                throw new WebApplicationException("A feed with the given name already exists: " + feed.getSystemName(), Status.BAD_REQUEST);
            }
        });
    }

    /**
     * Updates an existing feed.  Note that POST is used here rather than PUT since it behaves more like a PATCH; which isn't supported in Jersey.
     */
    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The feed was updated.", response = Feed.class),
                      @ApiResponse(code = 404, message = "The feed was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be updated.", response = RestResponseStatus.class)
                  })
    public Feed updateFeed(@PathParam("id") final String feedId, final Feed feed) {
        LOG.debug("Update feed: {}", feed);

        this.metadataTransform.validateCreate(feed);

        return this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

            if (domain != null) {
                domain = this.metadataTransform.updateDomain(feed, domain);
                return this.metadataTransform.domainToFeed().apply(domain);
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feed.getId(), Status.NOT_FOUND);
            }
        });
    }

    /**
     * Gets the properties for the specified feed.
     *
     * @param feedId the feed id or the feed category and name
     * @return the metadata properties
     */
    @GET
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the properties of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed properties.", response = Map.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed was not found.", response = RestResponseStatus.class)
                  })
    public Map<String, Object> getFeedProperties(@PathParam("id") final String feedId) {
        LOG.debug("Get feed properties ID: {}", feedId);

        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            String[] parts = feedId.split("\\.", 2);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = (parts.length == 2) ? feedProvider.findBySystemName(parts[0], parts[1]) : feedProvider.getFeed(feedProvider.resolveFeed(feedId));

            if (domain != null) {
                return domain.getProperties();
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    /**
     * Merges the properties for the specified feeds. New properties will be added and existing properties will be overwritten.
     *
     * @param feedId the feed id or the feed category and name
     * @param props  the properties to be merged
     * @return the merged metadata properties
     */
    @POST
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Merges the properties for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated properties.", response = Map.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The properties could not be updated.", response = RestResponseStatus.class)
                  })
    public Map<String, Object> mergeFeedProperties(@PathParam("id") final String feedId, final Properties props) {
        LOG.debug("Merge feed properties ID: {}, properties: {}", feedId, props);

        return this.metadata.commit(() -> {
            String[] parts = feedId.split("\\.", 2);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = (parts.length == 2) ? feedProvider.findBySystemName(parts[0], parts[1]) : feedProvider.getFeed(feedProvider.resolveFeed(feedId));

            if (domain != null) {
                return updateProperties(props, domain, false);
            } else {
                throw new WebApplicationException("No feed exist with the ID: " + feedId, Status.NOT_FOUND);
            }
        });
    }

    @PUT
    @Path("{id}/props")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Sets the properties for the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated properties.", response = Properties.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The properties could not be updated.", response = RestResponseStatus.class)
                  })
    public Properties replaceFeedProperties(@PathParam("id") final String feedId,
                                            final Properties props) {
        LOG.debug("Replace feed properties ID: {}, properties: {}", feedId, props);

        return this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainId = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed domain = feedProvider.getFeed(domainId);

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


    @GET
    @Path("{feedId}/lineage")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the lineage of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the feed lineage.", response = FeedLineage.class),
                      @ApiResponse(code = 400, message = "The id is not a valid UUID.", response = RestResponseStatus.class)
                  })
    public FeedLineage getFeedLineage(@PathParam("feedId") final String feedId) {

        return this.metadata.read(() -> {

            com.thinkbiganalytics.metadata.api.feed.Feed domainFeed = feedProvider.getFeed(feedProvider.resolveFeed(feedId));

            if (domainFeed != null) {
                FeedLineageBuilder builder = new FeedLineageBuilder(domainFeed, model, datasourceTransform);
                Feed feed = builder.build();//Model.DOMAIN_TO_FEED_WITH_DEPENDENCIES.apply(domainFeed);
                return new FeedLineage(feed, datasourceService.getFeedLineageStyleMap());
            }
            return null;
        });

    }

    /*
    @GET
    @Path("remove-sources")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponseStatus removeSources(){

        try {
            this.metadata.commit(() -> {
                List<? extends com.thinkbiganalytics.metadata.api.feed.Feed> feeds = feedProvider.getFeeds();
                feeds.stream().forEach(feed -> {
                    feedProvider.removeFeedSources(feed.getId());
                });
            }, MetadataAccess.SERVICE);
        }catch (Exception e){
            e.printStackTrace();
        }


        this.metadata.commit(() -> {
                                 List<? extends com.thinkbiganalytics.metadata.api.feed.Feed> feeds = feedProvider.getFeeds();
                                 feeds.stream().forEach(feed -> {
                                     feedProvider.removeFeedDestinations(feed.getId());
                                 });
                             }, MetadataAccess.SERVICE);
        this.metadata.commit(() -> {
            List<Datasource> datasources = datasetProvider.getDatasources();
            datasources.stream().forEach(datasource -> {
                datasetProvider.removeDatasource(datasource.getId());
            });

        }, MetadataAccess.SERVICE);
        return new RestResponseStatus.ResponseStatusBuilder().buildSuccess();

    }

*/


    @POST
    @Path("{feedId}/source")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Adds a source to the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated feed.", response = Feed.class),
                      @ApiResponse(code = 400, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be updated.", response = RestResponseStatus.class)
                  })
    public Feed addFeedSource(@PathParam("feedId") final String feedId,
                              @FormParam("datasourceId") final String datasourceId) {
        LOG.debug("Add feed source, feed ID: {}, datasource ID: {}", feedId, datasourceId);

        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
            com.thinkbiganalytics.metadata.api.feed.FeedSource domainDest = feedProvider.ensureFeedSource(domainFeedId, domainDsId);

            return this.metadataTransform.domainToFeed().apply(domainDest.getFeed());
        });
    }

    @POST
    @Path("{feedId}/destination")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Adds a destination to the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated feed.", response = Feed.class),
                      @ApiResponse(code = 400, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be updated.", response = RestResponseStatus.class)
                  })
    public Feed addFeedDestination(@PathParam("feedId") final String feedId,
                                   @FormParam("datasourceId") final String datasourceId) {
        LOG.debug("Add feed destination, feed ID: {}, datasource ID: {}", feedId, datasourceId);

        return this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            Datasource.ID domainDsId = datasetProvider.resolve(datasourceId);
            com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest = feedProvider.ensureFeedDestination(domainFeedId, domainDsId);

            return this.metadataTransform.domainToFeed().apply(domainDest.getFeed());
        });
    }

    @POST
    @Path("{feedId}/precondition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Adds a precondition to the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the updated feed.", response = Feed.class),
                      @ApiResponse(code = 400, message = "The feed could not be found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The feed could not be updated.", response = RestResponseStatus.class)
                  })
    public Feed setPrecondition(@PathParam("feedId") final String feedId, final FeedPrecondition precond) {
        LOG.debug("Add feed precondition, feed ID: {}, precondition: {}", feedId, precond);

        return this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID domainFeedId = feedProvider.resolveFeed(feedId);
            List<com.thinkbiganalytics.metadata.sla.api.Metric> domainMetrics
                = precond.getSla().getObligations().stream()
                .flatMap((grp) -> grp.getMetrics().stream())
                .map((metric) -> metric)
                .collect(Collectors.toList());

            com.thinkbiganalytics.metadata.api.feed.Feed domainFeed
                = feedProvider.createPrecondition(domainFeedId, "", domainMetrics);

            return this.metadataTransform.domainToFeed().apply(domainFeed);
        });
    }


    private Map<String, Object> updateProperties(final Properties props,
                                                 com.thinkbiganalytics.metadata.api.feed.Feed domain,
                                                 boolean replace) {
        return metadata.commit(() -> {
            Map<String, Object> newProperties = new HashMap<String, Object>();
            for (String name : props.stringPropertyNames()) {
                newProperties.put(name, props.getProperty(name));
            }
            if (replace) {
                feedProvider.replaceProperties(domain.getId(), newProperties);
            } else {
                feedProvider.mergeFeedProperties(domain.getId(), newProperties);
            }
            return domain.getProperties();
        });
    }

    private ServiceLevelAssessment generateModelAssessment(com.thinkbiganalytics.metadata.api.feed.FeedPrecondition precond) {
        com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assmt = this.preconditionService.assess(precond);
        return ServiceLevelAgreementModelTransform.DOMAIN_TO_SLA_ASSMT.apply(assmt);
    }

    private void ensurePrecondition(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
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

    private void ensureProperties(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domainFeed) {
        Map<String, Object> domainProps = domainFeed.getProperties();
        Properties props = feed.getProperties();

        for (String key : feed.getProperties().stringPropertyNames()) {
            domainProps.put(key, props.getProperty(key));
        }
    }

    private com.thinkbiganalytics.metadata.api.feed.FeedCriteria createFeedCriteria(String category,
                                                                                    String name,
                                                                                    String srcId,
                                                                                    String destId) {
        com.thinkbiganalytics.metadata.api.feed.FeedCriteria criteria = feedProvider.feedCriteria();

        if (StringUtils.isNotEmpty(category)) {
            criteria.category(category);
        }

        if (StringUtils.isNotEmpty(name)) {
            criteria.name(name);
        }
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
        List<com.thinkbiganalytics.metadata.api.feed.Feed> domainDeps = currentFeed.getDependentFeeds();
        FeedDependencyGraph feedDep = new FeedDependencyGraph(this.metadataTransform.domainToFeed().apply(currentFeed), null);

        if (!domainDeps.isEmpty()) {
            for (com.thinkbiganalytics.metadata.api.feed.Feed depFeed : domainDeps) {
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
