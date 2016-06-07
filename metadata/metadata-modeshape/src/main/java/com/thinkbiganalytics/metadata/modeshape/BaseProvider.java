package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Created by sr186054 on 6/5/16.
 */
public class BaseProvider {

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

    /**
     * Creates a new Entity Node object for a Parent Path, relative Path and node type
     * @param parentPath
     * @param relPath
     * @param type
     * @return
     */
    public Node createEntityNode( String parentPath, String relPath, String type) {
        Session session = getSession();

        try {
        Node typesNode = session.getNode(parentPath);
        JcrTools tools = new JcrTools();
        Node entNode =    tools.findOrCreateChild(typesNode, relPath, type);
        return entNode;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + type, e);
        }
    }



    public <T extends JcrEntity> T createEntity(String path, String relPath, String nodeType, Class<T> type,Map<String, Object> props) {
        return createEntity(path,relPath,nodeType,type,null,props);
    }

    public <T extends JcrEntity> T createEntity(String path, String relPath, String nodeType, Class<T> type, Object[] constructorArgs,Map<String, Object> props) {
        Session session = getSession();
            Node entNode =createEntityNode(path,relPath,nodeType);
            entNode = JcrUtil.setProperties(session, entNode, props);
           return JcrUtil.createJcrObject(entNode,type,constructorArgs);
    }

    public JcrFeed createFeed(GenericEntity.ID categoryId, Map<String,Object>props) {
        JcrCategory category = getEntity(categoryId,JcrCategory.class);
        String path = EntityUtil.pathForCategory(category.getSystemName());
        String systemName = (String)props.get(AbstractJcrSystemEntity.SYSTEM_NAME);
        Node feedNode = createEntityNode(path,systemName,JcrFeed.FEED_TYPE);
        JcrFeed feed = new JcrFeed(feedNode);
        feed.setSystemName(systemName);
        return feed;
    }

    public JcrCategory createCategory(Map<String, Object> props) {
        String path = EntityUtil.pathForCategory();
        String systemName = (String)props.get(AbstractJcrSystemEntity.SYSTEM_NAME);
        return createEntity(path,systemName, JcrCategory.CATEGORY_TYPE, JcrCategory.class, null,props);
    }




    public <T extends JcrEntity> T getEntity(GenericEntity.ID id, Class<T> type) {
        JcrEntity.EntityId idImpl = (JcrEntity.EntityId) id;

        try {
            Session session = getSession();
            Node node = session.getNodeByIdentifier(idImpl.getIdValue());

            if (node != null) {
             return JcrUtil.createJcrObject(node,type,null);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + idImpl.getIdValue(), e);
        }
    }

}
