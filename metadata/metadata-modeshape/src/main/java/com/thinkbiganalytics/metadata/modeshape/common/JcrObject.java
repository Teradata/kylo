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
