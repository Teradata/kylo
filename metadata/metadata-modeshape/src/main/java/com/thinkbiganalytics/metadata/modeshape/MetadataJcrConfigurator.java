/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.security.Privilege;

import org.modeshape.jcr.security.SimplePrincipal;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;

/**
 *
 * @author Sean Felten
 */
public class MetadataJcrConfigurator {
    
    @Inject
    private MetadataAccess metadataAccess;
    
    private List<PostMetadataConfigAction> postConfigActions = new ArrayList<>();
    
    public MetadataJcrConfigurator(List<PostMetadataConfigAction> actions) {
        this.postConfigActions.addAll(actions);
    }
    
    private final AtomicBoolean configured = new AtomicBoolean(false);
    
    
    public void configure() {
        this.metadataAccess.commit(() -> {
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                
                ensureLayout(session);
                ensureTypes(session);
                ensureAccessControl(session);
                this.configured.set(true);
                firePostConfigActions();
                return null;
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
            }
        }, MetadataAccess.SERVICE);
    }
    
    private void firePostConfigActions() {
        for (PostMetadataConfigAction action : this.postConfigActions) {
            // TODO: catch exceptions and continue?  Currently propagates runtime exceptions and will fail startup.
            action.run();
        }
    }

    public boolean isConfigured() {
        return this.configured.get();
    }

    private void ensureAccessControl(Session session) throws RepositoryException {
        if (! session.getRootNode().hasNode(SecurityPaths.SECURITY.toString())) {
            session.getRootNode().addNode(SecurityPaths.SECURITY.toString(), "tba:securityFolder");
        }

        Node prototypesNode = session.getRootNode().getNode(SecurityPaths.PROTOTYPES.toString());
        
        // Uncommenting below will remove all access control action configuration (DEV ONLY.)
        // TODO a proper migration should be implemented to in case the action hierarchy
        // has changed and the currently permitted actions need to be updated.
//        for (Node protoNode : JcrUtil.getNodesOfType(prototypesNode, "tba:allowedActions")) {
//            for (Node actionsNode : JcrUtil.getNodesOfType(protoNode, "tba:allowableAction")) {
//                actionsNode.remove();
//            }
//            
//            String modulePath = SecurityPaths.moduleActionPath(protoNode.getName()).toString();
//            
//            if (session.getRootNode().hasNode(modulePath)) {
//                Node moduleNode = session.getRootNode().getNode(modulePath);
//                
//                for (Node actionsNode : JcrUtil.getNodesOfType(moduleNode, "tba:allowableAction")) {
//                    actionsNode.remove();
//                }
//            }
//        }
        
        
        JcrAccessControlUtil.addPermissions(prototypesNode, new ModeShapeAdminPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(prototypesNode, AdminCredentials.getPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(prototypesNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
    }


    protected void ensureTypes(Session session) throws RepositoryException {
        Node typesNode = session.getRootNode().getNode(ExtensionsConstants.TYPES);
        NodeTypeManager typeMgr = session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
        NodeType extensionsType = typeMgr.getNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE);
        
        while (typeItr.hasNext()) {
            NodeType type = (NodeType) typeItr.next();
            
            if (type.isNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE) && 
                            ! type.equals(extensionsType) && 
                            ! typesNode.hasNode(type.getName())) {
                Node descrNode = typesNode.addNode(type.getName(), ExtensionsConstants.TYPE_DESCRIPTOR_TYPE);
                
                descrNode.setProperty("jcr:title", simpleName(type.getName()));
                descrNode.setProperty("jcr:description", "");
                
                PropertyDefinition[] defs = type.getPropertyDefinitions();
                
                for (PropertyDefinition def : defs) {
                    String fieldName = def.getName();
                    String prefix = namePrefix(fieldName);
                    
                    if (! ExtensionsConstants.STD_PREFIXES.contains(prefix) && ! descrNode.hasNode(fieldName)) {
                        Node propNode = descrNode.addNode(def.getName(), ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                        propNode.setProperty("jcr:title", def.getName().replace("^.*:", ""));
                        propNode.setProperty("jcr:description", "");
                    }
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
        
        if (! session.getRootNode().hasNode("users")) {
            session.getRootNode().addNode("users", "tba:usersFolder");
        }
        
        if (! session.getRootNode().hasNode("groups")) {
            session.getRootNode().addNode("groups", "tba:groupsFolder");
        }
        
        // TODO Temporary to cleanup schemas which had the category folder auto-created.
        if (session.getRootNode().hasNode("metadata/feeds/category")) {
            session.getRootNode().getNode("metadata/feeds/category").remove();
        }

        if (!session.getRootNode().hasNode("metadata/hadoopSecurityGroups")) {
            session.getRootNode().getNode("metadata").addNode("hadoopSecurityGroups");
        }
    }
    
    private String namePrefix(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);
        
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }
    
    private String simpleName(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);
        
        if (m.matches()) {
            return m.group(2);
        } else {
            return null;
        }
    }

}
