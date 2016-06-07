/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.Map;

import javax.jcr.nodetype.NodeType;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 * @author Sean Felten
 */
public class JcrExtensibleType implements ExtensibleType {
    
    private final NodeType nodeType;

    /**
     * 
     */
    public JcrExtensibleType(NodeType nodeDef) {
        this.nodeType = nodeDef;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getName()
     */
    @Override
    public String getName() {
        return getJcrName().replace(JcrMetadataAccess.TBA_PREFIX + ":", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getParentType()
     */
    @Override
    public ExtensibleType getParentType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getProperyTypes()
     */
    @Override
    public Map<String, ExtensibleType.PropertyType> getProperyTypes() {
        return JcrUtil.getPropertyTypes(this.nodeType);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleType#getPropertyType(java.lang.String)
     */
    @Override
    public ExtensibleType.PropertyType getPropertyType(String name) {
        return JcrUtil.getPropertyType(this.nodeType, name);
    }

    public String getJcrName() {
        return this.nodeType.getName();
    }

}
