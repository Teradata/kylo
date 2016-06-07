package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.poi.ss.formula.functions.T;
import org.modeshape.jcr.api.JcrTools;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 * Created by sr186054 on 6/5/16.
 */
public abstract class BaseJcrProvider<T, PK extends Serializable> implements BaseProvider<T, PK> {

    private Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }

    protected Class<T> entityClass;

    protected Class<? extends JcrEntity> jcrEntityClass;

    public abstract Class<? extends T> getEntityClass();

    public  abstract Class<? extends JcrEntity> getJcrEntityClass() ;

    /**
     * return the JCR NodeType for this entity (i.e. tba:category, tba:feed)
     * @return
     */
    public abstract String getNodeType();

    public BaseJcrProvider(){
        this.entityClass = (Class<T>)getEntityClass();
        this.jcrEntityClass = getJcrEntityClass();
    }

    /**
     * Creates a new Entity Node object for a Parent Path, relative Path and node type
     * @param parentPath
     * @param relPath
     * @return
     */
    public Node createEntityNode( String parentPath, String relPath) {
        Session session = getSession();

        try {
        Node typesNode = session.getNode(parentPath);
        JcrTools tools = new JcrTools();
        Node entNode =    tools.findOrCreateChild(typesNode, relPath, getNodeType());
        return entNode;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create new entity of type: " + getEntityClass(), e);
        }
    }



    public  T createEntity(String path, String relPath, Map<String, Object> props) {
        return createEntity(path, relPath, null, props);
    }

    public  T createEntity(String path, String relPath, Object[] constructorArgs,Map<String, Object> props) {
        Session session = getSession();
            Node entNode =createEntityNode(path,relPath);
            entNode = JcrUtil.setProperties(session, entNode, props);
           return (T)JcrUtil.createJcrObject(entNode, getJcrEntityClass(), constructorArgs);
    }




    @Override
    public T findById(PK id) {
        try {
        Node node =  getSession().getNodeByIdentifier(id.toString());
        if(node != null){
            return (T) constructEntity(node);
        }
            else {
            return null;
        }
    } catch (RepositoryException e) {
        throw new MetadataRepositoryException("Failure while finding entity by ID: " + id, e);
    }
    }

    protected T constructEntity(Node node){
        T entity = (T) JcrUtil.createJcrObject(node,getJcrEntityClass());
        return entity;
    }

    public List<T> find(String query) {
        List<T> entities = new ArrayList<>();
        JcrTools tools = new JcrTools();
        try {
            QueryResult result = tools.printQuery(getSession(), query);
            if(result != null) {
                NodeIterator nodeIterator = result.getNodes();
                while(nodeIterator.hasNext()){
                    Node node = nodeIterator.nextNode();
                    T entity = constructEntity(node);
                    entities.add(entity);
                }
            }
            return entities;
        }catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(), e);
        }
    }

    public T findFirst(String query) {
        JcrTools tools = new JcrTools();
        try {
            QueryResult result = tools.printQuery(getSession(), query);
            if(result != null) {
                NodeIterator nodeIterator = result.getNodes();
                if(nodeIterator.hasNext()){
                    Node node = nodeIterator.nextNode();
                    T entity = constructEntity(node);
                   return entity;
                }
            }
            return null;
        }catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(), e);
        }
    }

    @Override
    public List<T> findAll(){

        String jcrQuery = "SELECT * FROM ["+getNodeType()+"]";
     return find(jcrQuery);

    }

    @Override
    public T create(T t) {
        try {
            getSession().save();
            return t;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to save session", e);
        }
    }

    @Override
    public T update(T t) {
        try {
            getSession().save();
            return t;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to save session", e);
        }
    }

    @Override
    public void delete(T t) {
        if(t != null) {
            if (t instanceof JcrObject) {
                JcrObject jcrObject = (JcrObject) t;
                jcrObject.remove();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void deleteById(PK id) {
        T item = findById(id);
        delete(item);
    }


    @Override
    public PK resolveId(Serializable fid) {
        return (PK) new JcrEntity.EntityId(fid);
    }










}
