/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity.ID;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleEntityProvider implements ExtensibleEntityProvider {

    @Override
    public ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props) {
        JcrExtensibleType typeImpl = (JcrExtensibleType) type;
        Session session = getSession();
        
        try {
            Node entitiesNode = session.getNode("/" + ExtensionsConstants.ENTITIES);
            Node typesNode = null;
            
            if (entitiesNode.hasNode(typeImpl.getName())) {
                typesNode = entitiesNode.getNode(typeImpl.getJcrName());
            } else {
                typesNode = entitiesNode.addNode(typeImpl.getJcrName(), "nt:folder");
            }
            
            Node entNode = typesNode.addNode(typeImpl.getName() + "-" + UUID.randomUUID().toString(), typeImpl.getJcrName());
            entNode = JcrUtil.setProperties(session, entNode, props);
            
            return new JcrExtensibleEntity(entNode);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new extensible entity of type: " + typeImpl.getJcrName(), e);
        }
    }

    @Override
    public List<ExtensibleEntity> getEntities() {
        List<ExtensibleEntity> list = new ArrayList<>();
        Session session = getSession();
        
        try {
            Node genericsNode = session.getNode("/" + ExtensionsConstants.ENTITIES);
            NodeIterator typeNameItr = genericsNode.getNodes();
            
            while (typeNameItr.hasNext()) {
                Node typeNameNode = (Node) typeNameItr.next();
                NodeIterator entityItr = typeNameNode.getNodes();
                
                while (entityItr.hasNext()) {
                    Node entNode = (Node) entityItr.next();
                    list.add(new JcrExtensibleEntity(entNode));
                }
            }
            
            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve list of extensible entities", e);
        }
    }

    @Override
    public ExtensibleEntity getEntity(ID id) {
        JcrExtensibleEntity.EntityId idImpl = (JcrExtensibleEntity.EntityId) id;
        
        try {
            Session session = getSession();
            Node node = session.getNodeByIdentifier(idImpl.getIdValue());
            
            if (node != null) {
                return new JcrExtensibleEntity(node);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + idImpl.getIdValue(), e);
        }
    }

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

}
