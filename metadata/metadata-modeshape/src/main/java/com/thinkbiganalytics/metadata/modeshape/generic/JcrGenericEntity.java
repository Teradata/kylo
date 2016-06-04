/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrGenericEntity implements GenericEntity {

    private final Node node;
    
    /**
     * 
     */
    public JcrGenericEntity(Node node) {
        this.node = node;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getId()
     */
    @Override
    public ID getId() {
        try {
            return new EntityId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getTypeName()
     */
    @Override
    public String getTypeName() {
        try {
            return this.node.getPrimaryNodeType().getName().replace(JcrMetadataAccess.TBA_PREFIX + ":", "");
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity type name", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getPropertyValues()
     */
    @Override
    public Map<String, Object> getProperties() {
        return JcrUtil.getProperties(this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) {
        return JcrUtil.getProperty(this.node, name);
    }

    @Override
    public void setProperty(String name, Object value) {
        JcrUtil.setProperty(this.node, name, value);
    }

    
    protected static class EntityId implements ID {
        private static final long serialVersionUID = -9084653006891727475L;
        
        private String idValue;
        
        public EntityId(String idStr) {
            this.idValue = idStr;
        }
        
        public String getIdValue() {
            return idValue;
        }
    }

}
