/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.springframework.stereotype.Component;

import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;


@Component
@Path("/entity")
@Api(value = "jcr-entity-controller", produces = "application/json")
public class EntityController {

    @Inject
    private MetadataAccess metadata;

    @DELETE
    @Path("/{nodeId}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteJcrNode(@PathParam("nodeId") final String nodeId) {
        return metadata.commit(() -> {

            try {
                Session session = JcrMetadataAccess.getActiveSession();

                Node node = session.getNodeByIdentifier(nodeId);
                if(node != null) {
                    JcrEntity entity = JcrUtil.createJcrObject(node, JcrEntity.class);
                    Map<String,Object> objectMap = JcrUtil.jcrObjectAsMap(entity);
                    String json = com.thinkbiganalytics.json.ObjectMapperSerializer.serialize(objectMap);
                    node.remove();
                    return json;
                }else {
                    throw new RepositoryException("Unable to find Node for Id: "+nodeId);
                }
            } catch (Exception e) {
                throw new MetadataRepositoryException("Unable to remove Node for Id: " + nodeId, e);
            }


        });
    }


    
}
