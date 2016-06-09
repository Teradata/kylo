/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrPropertiesEntity extends JcrEntity implements Propertied {

    public static final String PROPERTIES_NAME = "tba:properties";
    /**
     *
     */
    public JcrPropertiesEntity(Node node) {
     super(node);
    }

    public JcrProperties getPropertiesObject() {
        return JcrUtil.getOrCreateNode(this.node, PROPERTIES_NAME, JcrProperties.NODE_TYPE, JcrProperties.class);
    }

    @Override
    /**
     * This will return just the extra properties.
     * All primary properties should be defined as getter/setter on the base object
     * You can call the getAllProperties to return the complete set of properties as a map
     */
    public Map<String,Object> getProperties(){

        JcrProperties props = getPropertiesObject();
        if(props != null) {
            return props.getProperties();
        }
        return null;
    }

    /**
     * Get the Nodes Properties along with the extra mixin properties
     */
    public Map<String, Object> getAllProperties() {

        //first get the other extra mixin properties
        Map<String, Object> properties = getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        //merge in this nodes properties
        Map<String, Object> thisProperties = super.getProperties();
        if (thisProperties != null)

        {
            properties.putAll(thisProperties);
        }
        return properties;
    }


    public void setProperties(Map<String,Object> properties){

        //add the properties as attrs
        for(Map.Entry<String,Object> entry: properties.entrySet()){
            setProperty(entry.getKey(), entry.getValue());
        }

    }


    /**
     * Override
     * if the incoming name matches that of a primary property on this Node then set it, otherwise add it the mixin bag of properties
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value) {
        try {
            if (JcrUtil.hasProperty(this.node.getPrimaryNodeType(), name)) {
                super.setProperty(name, value);
            } else {
                getPropertiesObject().setProperty(name, value);
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to set Property " + name + ":" + value);
        }
    }

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        return null;
    }

    @Override
    public void removeProperty(String key) {

    }
}
