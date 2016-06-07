/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.api.Propertied;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.Map;

import javax.jcr.Node;

/**
 *
 * @author Sean Felten
 */
public class JcrPropertiesEntity extends JcrEntity implements Propertied {

    public static final String PROPERTIES_NAME = "tba:properties";
    public static final String PROPERTIES_TYPE = "tba:properties";
    /**
     *
     */
    public JcrPropertiesEntity(Node node) {
     super(node);
    }


    @Override
    public Map<String,Object> getProperties(){

        JcrProperties props = JcrUtil.getNode(this.node,PROPERTIES_NAME,JcrProperties.class);
        if(props != null) {
            return props.getProperties();
        }
        return null;
    }


    public void setProperties(Map<String,Object> properties){

        JcrProperties n = JcrUtil.getOrCreateNode(this.node, PROPERTIES_NAME, PROPERTIES_TYPE, JcrProperties.class);
        //add the properties as attrs
        for(Map.Entry<String,Object> entry: properties.entrySet()){
            n.setProperty(entry.getKey(),entry.getValue());
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
