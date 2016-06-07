/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity.ID;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleEntityProvider implements ExtensibleEntityProvider {

    /**
     * 
     */
    public JcrExtensibleEntityProvider() {
    }

    @Override
    public ExtensibleType createType(String name, Map<String, ExtensibleType.PropertyType> props) {
        return createType(name, null, props);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExtensibleType createType(String name, ExtensibleType supertype, Map<String, ExtensibleType.PropertyType> props) {
        try {
            Session session = getSession();
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeTypeTemplate nodeTemplate = typeMgr.createNodeTypeTemplate();
            nodeTemplate.setName(JcrMetadataAccess.TBA_PREFIX + ":" + name);
            
            if (supertype != null) {
                JcrExtensibleType superImpl = (JcrExtensibleType) supertype;
                String supername = superImpl.getJcrName();
                nodeTemplate.setDeclaredSuperTypeNames(new String[] { "tba:genericEntity", supername });
            } else {
                nodeTemplate.setDeclaredSuperTypeNames(new String[] { "tba:genericEntity" });
            }
            
            for (Entry<String, ExtensibleType.PropertyType> entry : props.entrySet()) {
                PropertyDefinitionTemplate propDef = typeMgr.createPropertyDefinitionTemplate();
                propDef.setName(entry.getKey());
                propDef.setRequiredType(JcrUtil.asCode(entry.getValue()));
                nodeTemplate.getPropertyDefinitionTemplates().add(propDef);
            }
            
            NodeType nodeType = typeMgr.registerNodeType(nodeTemplate, true);
            
            if (! session.nodeExists("/metadata/generic/entities/" + name)) {
                session.getRootNode().addNode("metadata/generic/entities/" + name, "nt:folder");
            }
            
            return new JcrExtensibleType(nodeType);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new generic type: " + name, e);
        }
    }
    
    @Override
    public ExtensibleType getType(String name) {
        Session session = getSession();
        try {
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeType nodeType = typeMgr.getNodeType(JcrMetadataAccess.TBA_PREFIX + ":" + name);
            return new JcrExtensibleType(nodeType);
        } catch (NoSuchNodeTypeException e) {
            return null;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to lookup generic type: " + name, e);
        }
    }
    
    @Override
    public List<ExtensibleType> getTypes() {
        Session session = getSession();
        try {
            List<ExtensibleType> list = new ArrayList<ExtensibleType>();
            NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
            NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
            NodeType genericType = typeMgr.getNodeType("tba:genericEntity");
            
            while (typeItr.hasNext()) {
                NodeType nodeType = (NodeType) typeItr.next();
                
                if (nodeType.isNodeType(genericType.getName()) && ! nodeType.equals(genericType)) {
                    list.add(new JcrExtensibleType(nodeType));
                }
            }
            
            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to lookup all generic types", e);
        }
    }
    
    @Override
    public ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props) {
        JcrExtensibleType typeImpl = (JcrExtensibleType) type;
        Session session = getSession();
        
        try {
            Node typesNode = session.getNode(Paths.get("/metadata", "generic", "entities", typeImpl.getName()).toString());
            Node entNode = typesNode.addNode(UUID.randomUUID().toString(), typeImpl.getJcrName());
            entNode = JcrUtil.setProperties(session, entNode, props);
            
            return new JcrExtensibleEntity(entNode);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new generic entity of type: " + typeImpl.getName(), e);
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
    
    @Override
    public List<ExtensibleEntity> getEntities() {
        List<ExtensibleEntity> list = new ArrayList<>();
        Session session = getSession();
        
        try {
            Node genericsNode = session.getNode("/metadata/generic/entities");
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
            throw new MetadataRepositoryException("Failed to retrieve list of generic entities", e);
        }
    }

    /**
     * Return the Property names and types for a given NodeType (i.e. pass in tba:feed)
     * @param nodeType
     * @return
     * @throws RepositoryException
     */
    public Map<String,ExtensibleType.PropertyType> getPropertyTypes(String nodeType) {
        try {
        NodeTypeManager typeMgr = getSession().getWorkspace().getNodeTypeManager();
        NodeType type = typeMgr.getNodeType(nodeType);
            return JcrUtil.getAllPropertyTypes(type, false);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve list of property types for node "+nodeType, e);
        }
    }

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

}
