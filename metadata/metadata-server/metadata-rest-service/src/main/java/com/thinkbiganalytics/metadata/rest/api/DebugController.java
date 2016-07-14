/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceScheduleMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.metadata.rest.model.sla.WithinSchedule;

import org.modeshape.jcr.api.JcrTools;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Session;
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

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/debug")
public class DebugController {
    
    @Context
    private UriInfo uriInfo;
    
    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private MetadataEventService eventService;

    @POST
    @Path("event")
    public String postFeedOperationStatusEvent(@QueryParam("feed") String feedName, 
                                               @QueryParam("op") String opIdStr, 
                                               @QueryParam("state") String stateStr,
                                               @QueryParam("status") @DefaultValue("") String status) {
        FeedOperation.ID opId = null;
        FeedOperation.State state = FeedOperation.State.valueOf(stateStr.toUpperCase());
        FeedOperationStatusEvent event = new FeedOperationStatusEvent(feedName, opId, state, status);
        
        this.eventService.notify(event);
        
        return event.toString();
    }
    
    @GET
    @Path("metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Metric> exampleMetrics() {

        return Arrays.asList(FeedExecutedSinceFeedMetric.named("category", "DependentFeed", "ExecutedSinceCategory", "ExecutedSinceFeed"),
                             FeedExecutedSinceScheduleMetric.named("category", "Feed", "* * * * * ? *"),
                             new WithinSchedule("* * * * * ? *", "4 hours"));
    }
    
    @GET
    @Path("procondition")
    @Produces(MediaType.APPLICATION_JSON)
    public FeedPrecondition examplePrecondition() {
        FeedPrecondition procond = new FeedPrecondition("DependingPrecondition");
        procond.addMetrics("Feed dependson on execution of another feed",
                           FeedExecutedSinceFeedMetric.named("DependentCategory", "DependentFeed", "ExecutedSinceCategory", "ExecutedSinceFeed"));
        return procond;
    }
    
    @GET
    @Path("jcr/{abspath: .*}")
    @Produces(MediaType.TEXT_PLAIN)
    public String printJcrTree(@PathParam("abspath") final String abspath) {
        return metadata.read(() -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                Node node = session.getNode("/" + abspath);
                JcrTools tools = new JcrTool(true, pw);
                tools.printSubgraph(node);
            } catch (Exception e) {
                e.printStackTrace(pw);
            }
            
            pw.flush();
            return sw.toString();
        });
    }
    
}
