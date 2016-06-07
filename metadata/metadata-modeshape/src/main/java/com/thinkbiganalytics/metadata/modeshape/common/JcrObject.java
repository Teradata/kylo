package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/6/16.
 */
public class JcrObject {


    protected final Node node;

    /**
     *
     */
    public JcrObject(Node node) {
        this.node = node;
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
            throw new MetadataRepositoryException("Unable to get the Path", e);
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
        return JcrUtil.getProperties(this.node);
    }

    public Object getProperty(String name) {
        return JcrUtil.getProperty(this.node, name);
    }

    public <T> T getProperty(String name, Class<T> type) {
        return (T) JcrUtil.getProperty(this.node, name);
    }

    public void setProperty(String name, Object value) {
        JcrUtil.setProperty(this.node, name, value);
    }






}
