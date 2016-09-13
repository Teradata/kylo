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

import org.springframework.stereotype.Component;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ContentType;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/dataop")
public class DataOperationsController {
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DataOperationsProvider operationsProvider;
    
    @Inject
    private MetadataAccess metadata;

    /**
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataOperation> getOperations() {
        return this.metadata.read(() -> {
            // TODO add criteria filtering
            List<com.thinkbiganalytics.metadata.api.op.DataOperation> domainOps = operationsProvider.getDataOperations();
            
            return new ArrayList<>(Collections2.transform(domainOps, Model.DOMAIN_TO_DS_OP));
        });
    }

    @GET
    @Path("{opid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation getOperation(@PathParam("opid") final String opid) {
        
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.op.DataOperation.ID domainId = operationsProvider.resolve(opid);
            com.thinkbiganalytics.metadata.api.op.DataOperation domainOp = operationsProvider.getDataOperation(domainId);
            
            if (domainOp != null) {
                return Model.DOMAIN_TO_DS_OP.apply(domainOp);
            } else {
                throw new WebApplicationException("No data operation exitst with the given ID: " + opid, Status.NOT_FOUND);
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation beginOperation(@FormParam("feedDestinationId") final String destIdStr, 
                                        @FormParam("status") final String status) {
        
        return this.metadata.read(() -> {
                // Deprecated
//                com.thinkbiganalytics.metadata.api.feed.FeedDestination.ID destId = feedProvider.resolveDestination(destIdStr);
//                com.thinkbiganalytics.metadata.api.feed.FeedDestination dest = feedProvider.getFeedDestination(destId);
//                
//                if (dest != null) {
//                    com.thinkbiganalytics.metadata.api.op.DataOperation op = operationsProvider.beginOperation(dest, new DateTime());
//                    return Model.DOMAIN_TO_DS_OP.apply(op);
//                } else {
//                    throw new WebApplicationException("A feed destination with the given ID does not exist: " + destIdStr, 
//                                                      Status.BAD_REQUEST);
//                }
            return null;
        });
    }
    
    @PUT
    @Path("{opid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataOperation updateOperation(@PathParam("opid") final String opid,
                                         final DataOperation op) {
        
        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.op.DataOperation.ID domainId = operationsProvider.resolve(opid);
            com.thinkbiganalytics.metadata.api.op.DataOperation domainOp = operationsProvider.getDataOperation(domainId);
            Datasource domainDs = domainOp.getProducer().getDatasource();
            com.thinkbiganalytics.metadata.api.op.DataOperation resultOp;
            
            if (op.getState() == State.SUCCESS) {
                if (domainDs instanceof HiveTableDatasource && op.getDataset().getContentType() == ContentType.PARTITIONS) {
                    // TODO Handle partitions
                    Dataset<HiveTableDatasource, HiveTableUpdate> change = operationsProvider.createDataset((HiveTableDatasource) domainDs, 0);
                    resultOp = operationsProvider.updateOperation(domainId, "", change);
                } else if (domainDs instanceof DirectoryDatasource && op.getDataset().getContentType() == ContentType.FILES) {
                    List<java.nio.file.Path> paths = new ArrayList<>();
                    
                    for (com.thinkbiganalytics.metadata.rest.model.op.ChangeSet cs : op.getDataset().getChangeSets()) {
                        com.thinkbiganalytics.metadata.rest.model.op.FileList fl = (com.thinkbiganalytics.metadata.rest.model.op.FileList) cs;
                        for (String pathStr : fl.getPaths()) {
                            paths.add(java.nio.file.Paths.get(pathStr));
                        }
                    }
                    
                    Dataset<DirectoryDatasource, FileList> change = operationsProvider.createDataset((DirectoryDatasource) domainDs, paths);
                    resultOp = operationsProvider.updateOperation(domainId, "", change);
                } else {
                    resultOp = operationsProvider.updateOperation(domainId, op.getStatus(), Model.OP_STATE_TO_DOMAIN.apply(op.getState()));
                }
            } else {
                resultOp = operationsProvider.updateOperation(domainId, op.getStatus(), Model.OP_STATE_TO_DOMAIN.apply(op.getState()));
            }
            
            return Model.DOMAIN_TO_DS_OP.apply(resultOp);
        });
    }
}
