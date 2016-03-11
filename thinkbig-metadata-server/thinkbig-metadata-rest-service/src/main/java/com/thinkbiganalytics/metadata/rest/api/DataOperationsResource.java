/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ContentType;

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

    @GET
    @Path("{opid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation getOperation(@PathParam("opid") String opid) {
        com.thinkbiganalytics.metadata.api.op.DataOperation.ID domainId = this.operationsProvider.asOperationId(opid);
        com.thinkbiganalytics.metadata.api.op.DataOperation domainOp = this.operationsProvider.getDataOperation(domainId);
        
        if (domainOp != null) {
            return Model.DOMAIN_TO_OP.apply(domainOp);
        } else {
            throw new WebApplicationException("No data operation exitst with the given ID: " + opid, Status.NOT_FOUND);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation beginOperation(@FormParam("feedDestinationId") String destIdStr, 
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
    
    @PUT
    @Path("{opid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation updateOperation(@PathParam("opid") String opid,
                                         DataOperation op) {
        com.thinkbiganalytics.metadata.api.op.DataOperation.ID domainId = this.operationsProvider.asOperationId(opid);
        com.thinkbiganalytics.metadata.api.op.DataOperation domainOp = this.operationsProvider.getDataOperation(domainId);
        Dataset domainDs = domainOp.getProducer().getDataset();
        com.thinkbiganalytics.metadata.api.op.DataOperation resultOp;
        
        if (op.getState() == State.SUCCESS) {
            if (domainDs instanceof HiveTableDataset && op.getDataset().getContentType() == ContentType.PARTITIONS) {
                // TODO Handle partitions
                ChangeSet<HiveTableDataset, HiveTableUpdate> change = this.operationsProvider.createChangeSet((HiveTableDataset) domainDs, 0);
                resultOp = this.operationsProvider.updateOperation(domainId, "", change);
            } else if (domainDs instanceof DirectoryDataset && op.getDataset().getContentType() == ContentType.FILES) {
                List<java.nio.file.Path> paths = new ArrayList<>();
                
                for (com.thinkbiganalytics.metadata.rest.model.op.ChangeSet cs : op.getDataset().getChangeSets()) {
                    com.thinkbiganalytics.metadata.rest.model.op.FileList fl = (com.thinkbiganalytics.metadata.rest.model.op.FileList) cs;
                    for (String pathStr : fl.getPaths()) {
                        paths.add(java.nio.file.Paths.get(pathStr));
                    }
                }
                
                ChangeSet<DirectoryDataset, FileList> change = this.operationsProvider.createChangeSet((DirectoryDataset) domainDs, paths);
                resultOp = this.operationsProvider.updateOperation(domainId, "", change);
            } else {
                resultOp = this.operationsProvider.updateOperation(domainId, op.getStatus(), Model.OP_STATE_TO_DOMAIN.apply(op.getState()));
            }
        } else {
            resultOp = this.operationsProvider.updateOperation(domainId, op.getStatus(), Model.OP_STATE_TO_DOMAIN.apply(op.getState()));
        }

        return Model.DOMAIN_TO_OP.apply(resultOp);
    }
}
