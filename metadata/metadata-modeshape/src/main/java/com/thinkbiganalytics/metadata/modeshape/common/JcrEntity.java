/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

import com.thinkbiganalytics.metadata.api.generic.GenericEntity;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.modeshape.jcr.api.JcrTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author Sean Felten
 */
public class JcrEntity  extends JcrObject implements GenericEntity {

    /**
     *
     */
    public JcrEntity(Node node) {
     super(node);
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



    
    public static class EntityId implements ID {
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
