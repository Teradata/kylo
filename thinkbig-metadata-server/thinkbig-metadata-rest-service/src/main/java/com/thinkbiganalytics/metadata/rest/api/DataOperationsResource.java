/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/dataop")
public class DataOperationsResource {
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DataOperationsProvider operationsProvider;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation beginOperation(@PathParam("feedDestinationId") String destIdStr, 
                                        @FormParam("status") String status) {
        
        FeedDestination d;
        com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID destId = this.feedProvider.resolveDestination(destIdStr);
        com.thinkbiganalytics.metadata.api.feed.FeedDestination dest = this.feedProvider.getFeedDestination(destId);
        
        if (dest != null) {
            com.thinkbiganalytics.metadata.api.op.DataOperation op = this.operationsProvider.beginOperation(dest, new DateTime());
            return Model.DOMAIN_TO_OP.apply(op);
        } else {
            throw new WebApplicationException("A feed destination with the given ID does not exist: " + destIdStr, 
                                              Status.BAD_REQUEST);
        }
    }
}
