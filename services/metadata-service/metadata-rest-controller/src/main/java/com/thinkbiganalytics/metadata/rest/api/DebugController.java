package com.thinkbiganalytics.metadata.rest.api;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceSchedule;
import com.thinkbiganalytics.metadata.api.sla.WithinSchedule;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.modeshape.jcr.api.JcrTools;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * A controller to use when debugging issues with the UI
 */
@Api(tags = "Internal", produces = "application/json")
@Component
@Path("/v1/metadata/debug")
@SwaggerDefinition(tags = @Tag(name = "Internal", description = "debugging tools"))
public class DebugController {

    @Context
    private UriInfo uriInfo;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private MetadataEventService eventService;

    /**
     * Allows the caller to update status events for the feed
     *
     * @param feedName the name of the feed
     * @param opIdStr  the operation for the feed
     * @param stateStr the new state to persist
     * @param status   the status of the operation
     * @return the feed operation status event
     */
    @POST
    @Path("feedop/event")
    public String postFeedOperationStatusEvent(@QueryParam("feed") String feedName,
                                               @QueryParam("op") String opIdStr,
                                               @QueryParam("state") String stateStr,
                                               @QueryParam("status") @DefaultValue("") String status) {
        FeedOperation.ID opId = null;
        FeedOperation.State state = FeedOperation.State.valueOf(stateStr.toUpperCase());
        OperationStatus opStatus = new OperationStatus(feedName, opId, state, status);
        FeedOperationStatusEvent event = new FeedOperationStatusEvent(opStatus);

        this.eventService.notify(event);

        return event.toString();
    }

    /**
     * creates a hive table model object for debugging
     *
     * @return the hive table data source model
     */
    @GET
    @Path("datasource/hivetable")
    @Produces(MediaType.APPLICATION_JSON)
    public Datasource exampleHiveTable() {
        return new HiveTableDatasource("table1", "database1", "table1");
    }

    /**
     * returns a list of metrics for debugging
     *
     * @return a list of metrics
     */
    @GET
    @Path("metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Metric> exampleMetrics() {
        List<Metric> metrics = new ArrayList<>();
        FeedExecutedSinceSchedule feedExecutedSinceSchedule = null;
        try {
            feedExecutedSinceSchedule = new FeedExecutedSinceSchedule("category", "Feed", "* * * * * ? *");
            metrics.add(feedExecutedSinceSchedule);

            WithinSchedule withinSchedule = new com.thinkbiganalytics.metadata.api.sla.WithinSchedule("* * * * * ? *", "4 hours");
            metrics.add(withinSchedule);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return metrics;
    }

    /**
     * returns a new feed precondition model for debugging
     *
     * @return the precondition model
     */
    @GET
    @Path("precondition")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedPrecondition examplePrecondition() {
        FeedPrecondition procond = new FeedPrecondition("DependingPrecondition");
        procond.addMetrics("Feed dependson on execution of another feed",
                           new FeedExecutedSinceFeed("DependentCategory", "DependentFeed", "ExecutedSinceCategory", "ExecutedSinceFeed"));
        return procond;
    }

    /**
     * delete the JCR tree specified by the path
     *
     * @param abspath the path with JCR to delete
     * @return a confirmation message that the path was deleted
     */
    @DELETE
    @Path("jcr/{abspath: .*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteJcrTree(@PathParam("abspath") final String abspath) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        try {
            metadata.commit(() -> {
                Session session = JcrMetadataAccess.getActiveSession();
                session.removeItem("/" + abspath);
                pw.print("DELETED " + abspath);
            });
        } catch (Exception e) {
            e.printStackTrace(pw);
            throw new RuntimeException(e);
        }

        pw.flush();
        return sw.toString();
    }

    /**
     * prints the nodes of the JCR path given, for debugging
     *
     * @param abspath the path in JCR
     * @return a printout of the JCR tree
     */
    @GET
    @Path("jcr/{abspath: .*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String printJcrTree(@PathParam("abspath") final String abspath) {

        return metadata.read(() -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Node node = session.getRootNode().getNode(abspath);
                JcrTools tools = new JcrTool(true, pw);
                tools.printSubgraph(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            pw.flush();
            return sw.toString();
        });
    }


    /**
     * Prints the subgraph of the node in JCR
     *
     * @param jcrId the id of the node in JCR
     * @return the subgraph print out
     */
    @GET
    @Path("jcr")
    @Produces(MediaType.TEXT_PLAIN)
    public String printJcrId(@QueryParam("id") final String jcrId) {
        return metadata.read(() -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Node node = session.getNodeByIdentifier(jcrId);
                pw.print("Path: ");
                pw.println(node.getPath());
                JcrTools tools = new JcrTool(true, pw);
                tools.printSubgraph(node);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            pw.flush();
            return sw.toString();
        });
    }

}
