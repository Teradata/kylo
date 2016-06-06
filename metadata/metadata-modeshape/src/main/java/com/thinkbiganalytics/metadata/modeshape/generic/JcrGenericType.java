/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

import java.util.Map;

import javax.jcr.nodetype.NodeType;

import com.thinkbiganalytics.metadata.api.generic.GenericType;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public class JcrGenericType implements GenericType {
    
    private final NodeType nodeType;

    /**
     * 
     */
    public JcrGenericType(NodeType nodeDef) {
        this.nodeType = nodeDef;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.generic.GenericType#getName()
     */
    @Override
    public String getName() {
        return getJcrName().replace(JcrMetadataAccess.TBA_PREFIX + ":", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.generic.GenericType#getParentType()
     */
    @Override
    public GenericType getParentType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.generic.GenericType#getProperyTypes()
     */
    @Override
    public Map<String, GenericType.PropertyType> getProperyTypes() {
        return JcrUtil.getPropertyTypes(this.nodeType);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.generic.GenericType#getPropertyType(java.lang.String)
     */
    @Override
    public GenericType.PropertyType getPropertyType(String name) {
        return JcrUtil.getPropertyType(this.nodeType, name);
    }

    public String getJcrName() {
        return this.nodeType.getName();
    }

}
