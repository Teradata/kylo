package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.UnknownPropertyException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/6/16.
 */
public class JcrObject {

    protected final Node node;

    private String versionName;

    private String versionableIdentifier;

    public String getObjectId() throws RepositoryException {
        if (this.node.isNodeType("nt:frozenNode")) {
            return this.versionableIdentifier;
        } else {
            return this.node.getIdentifier();
        }
    }


    /**
     *
     */
    public JcrObject(Node node) {
        this.node = node;
        String nodeName = null;
        try {
            nodeName = node.getName();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to create JcrObject from node " + nodeName, e);
        }
    }

    public String getTypeName() {
        try {
            return this.node.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity type name", e);
        }
    }

    public boolean isNew(){
        return this.node.isNew();
    }

    public boolean isModified(){
        return this.node.isModified();
    }

    public void refresh(boolean keepChanges) {
        try {
            this.node.refresh(keepChanges);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to refresh Node. ",e);
        }
    }

    public String getPath() {
        try {
            return this.node.getPath();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get the Path", e);
        }
    }

    public String getNodeName(){
        try {
        return this.node.getName();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to get the Node Name", e);
        }
    }

    public void remove(){
        try {
            this.node.remove();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to remove the node", e);
        }
    }

    public boolean isLive(){
        if(this.node != null) {
            try {
                if(this.node.getSession() != null) {
                    return this.node.getSession().isLive();
                }
            } catch (RepositoryException e) {

            }
        }
        return false;
    }

    public Map<String, Object> getProperties() {
        return JcrPropertyUtil.getProperties(this.node);
    }

    public Object getProperty(String name) {
        return JcrPropertyUtil.getProperty(this.node, name);
    }

    public <T> Set<T> getPropertyAsSet(String name, Class<T> objectType) {
        Object o = null;
        try {
            o = JcrPropertyUtil.getProperty(this.node, name);
        } catch (UnknownPropertyException e) {

        }
        if (o != null) {
            if (o instanceof Collection) {
                return new HashSet<T>((Collection) o);
            } else {
                Set<T> set = new HashSet<>();
                set.add((T) o);
                return set;
            }
        }
        return new HashSet<T>();
    }
    public <T> T getProperty(String name, Class<T> type) {
        return getProperty(name, type, false);
    }

    public <T> T getProperty(String name, Class<T> type,boolean allowNotFound) {
      return getPropertyFromNode(this.node,name,type,allowNotFound);
    }

    protected <T> T getPropertyFromNode(Node node, String name, Class<T> type, boolean allowNotFound){
        Object o = JcrPropertyUtil.getProperty(node, name,allowNotFound);
        if(allowNotFound && o == null){
            return null;
        }
        if (type.isEnum()) {
            String savedType = o.toString();
            if (StringUtils.isNotBlank(savedType)) {
                Class<? extends Enum> x = (Class<? extends Enum>) type;
                return (T) Enum.valueOf(x, savedType);
            }
        }
        if (!o.getClass().isAssignableFrom(type)) {
            //if it cant be matched and it is a Node > JcrObject, do the conversion
            if (o instanceof Node && JcrObject.class.isAssignableFrom(type)) {
                return JcrUtil.constructNodeObject((Node) o, type, null);
            } else {
                throw new MetadataRepositoryException("Unable to convert Property " + name + " to type " + type);
            }
        } else {
            return (T) o;
        }
    }

    public void setProperty(String name, Object value) {
        JcrPropertyUtil.setProperty(this.node, name, value);
    }


    public Node getNode() {
        return this.node;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionableIdentifier() {
        return versionableIdentifier;
    }

    public void setVersionableIdentifier(String versionableIdentifier) {
        this.versionableIdentifier = versionableIdentifier;
    }
}
