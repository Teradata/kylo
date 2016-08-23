/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Named;
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

import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;
import com.thinkbiganalytics.security.UserRolePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 * @author Sean Felten
 */
public class MetadataJcrConfigurator {
    
    @Inject
    private JcrMetadataAccess metadataAccess;
    
//    @Inject
//    @Named("servicesPrototypeAllowedActions")
//    private AllowedActions servicesPrototypeActions;
    
    private final AtomicBoolean configured = new AtomicBoolean(false);
    
    
    public void configure() {
        this.metadataAccess.commit(new AdminCredentials(), () -> {
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                
                ensureLayout(session);
                ensureTypes(session);
                ensureAccessControl(session);
                this.configured.set(true);
                return null;
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
            }
        });
    }
    
    public boolean isConfigured() {
        return this.configured.get();
    }

    private void ensureAccessControl(Session session) throws RepositoryException {
        if (! session.getRootNode().hasNode(SecurityPaths.SECURITY.toString())) {
            session.getRootNode().addNode(SecurityPaths.SECURITY.toString(), "tba:securityFolder");
        }

        Node protoNode = session.getRootNode().getNode(SecurityPaths.PROTOTYPES.toString());
        Path svcPath = SecurityPaths.moduleActionPath("services");
        
        JcrAccessControlUtil.addPermissions(protoNode, new ModeShapeAdminPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(protoNode, AdminCredentials.getPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(protoNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
        
//        if (session.getRootNode().hasNode(svcPath.toString())) {
//            session.getRootNode().getNode(svcPath.toString()).remove();
//        }
//       
//        session.getWorkspace().copy("/" + SecurityPaths.prototypeActionsPath("services").toString(), 
//                                    svcPath.toString());
//
//        JcrAccessControlUtil.addPermissions(session.getRootNode().getNode(svcPath.toString()), new ModeShapeAdminPrincipal(), Privilege.JCR_ALL);
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
        
        // TODO Temporary to cleanup schemas which had the category folder auto-created.
        if (session.getRootNode().hasNode("metadata/feeds/category")) {
            session.getRootNode().getNode("metadata/feeds/category").remove();
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
