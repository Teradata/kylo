package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.reflect.ConstructorUtils;

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

    public Node createEntityNode( String path, String name) {
        Session session = getSession();

        try {
        Node typesNode = session.getNode(path);
        Node entNode = typesNode.addNode(UUID.randomUUID().toString(), name);
        return entNode;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + name, e);
        }
    }


    public <T extends JcrEntity> T createEntity(String path, String name, Class<T> type, Object[] constructorArgs,Map<String, Object> props) {
        Session session = getSession();

        try {
            Node entNode =createEntityNode(path,name);
            entNode = JcrUtil.setProperties(session, entNode, props);
            T entity = ConstructorUtils.invokeConstructor(type, entNode);
            return entity;
        } catch ( InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + name, e);
        }
    }

    public JcrFeed createFeed(GenericEntity.ID categoryId) {
        String path = Paths.get(EntityUtil.pathForCategory(), categoryId.toString(), "feed").toString();
        //fetch the category
        JcrCategory category = getEntity(categoryId,JcrCategory.class);
        return createEntity(path, "tba:feed", JcrFeed.class,null,null);
    }

    public JcrCategory createCategory(Map<String, Object> props) {
        String path = EntityUtil.pathForCategory();
        return createEntity(path, "tba:category", JcrCategory.class, null,props);
    }




    public <T extends JcrEntity> T getEntity(GenericEntity.ID id, Class<T> type) {
        JcrEntity.EntityId idImpl = (JcrEntity.EntityId) id;

        try {
            Session session = getSession();
            Node node = session.getNodeByIdentifier(idImpl.getIdValue());

            if (node != null) {
                T entity = ConstructorUtils.invokeConstructor(type, node);
                return entity;
            } else {
                return null;
            }
        } catch (RepositoryException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new MetadataRepositoryException("Failure while finding entity by ID: " + idImpl.getIdValue(), e);
        }
    }

}
