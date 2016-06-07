/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;

/**
 *
 * @author Sean Felten
 */
public class MetadataJcrConfigurator {
    
    @Inject
    private MetadataAccess metadataAccess;
    
    public void configure() {
        this.metadataAccess.commit(new Command<String>() {
            @Override
            public String execute() {
                try {
                    Session session = JcrMetadataAccess.getActiveSession();
                    
                    ensureLayout(session);
                    ensureTypes(session);
                    return null;
                } catch (RepositoryException e) {
                    throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
                }
            }
        });
    }
    

    protected void ensureTypes(Session session) throws RepositoryException {
        Node typesNode = session.getRootNode().getNode("/metadata/generic/types");
        NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
        
        while (typeItr.hasNext()) {
            NodeType type = (NodeType) typeItr.next();
            
            if (! typesNode.hasNode(type.getName())) {
                Node descrNode = typesNode.addNode(type.getName());
                descrNode.setProperty("jcr:title", type.getName().replaceAll("^.*:", ""));
                descrNode.setProperty("jcr:description", "");
                
                PropertyDefinition[] defs = type.getPropertyDefinitions();
                
                for (PropertyDefinition def : defs) {
                    Node propNode = descrNode.addNode(def.getName());
                    propNode.setProperty("jcr:title", def.getName().replace("^.*:", ""));
                    propNode.setProperty("jcr:description", "");
                }
            }
        }
        
        NodeIterator nodeItr = typesNode.getNodes();
        
        while (nodeItr.hasNext()) {
            Node typeNode = (Node) nodeItr.next();
            
            if (! typeMgr.hasNodeType(typeNode.getName())) {
                typeNode.remove();
            }
        }
    }


    protected void ensureLayout(Session session) throws RepositoryException {
        if (! session.getRootNode().hasNode("metadata")) {
            session.getRootNode().addNode("metadata", "tba:metadataFolder");
        }
    }
}
