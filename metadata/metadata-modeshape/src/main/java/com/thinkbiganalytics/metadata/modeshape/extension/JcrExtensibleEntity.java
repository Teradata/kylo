/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.extension;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;

import java.io.Serializable;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrExtensibleEntity extends JcrEntity<JcrExtensibleEntity.EntityId> implements AuditableMixin, ExtensibleEntity {


    /**
     *
     */
    public JcrExtensibleEntity(Node node) {
        super(node);
    }
    
    @Override
    public EntityId getId() {
        try {
            return new EntityId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity#getPropertySet(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> Set<T> getPropertySet(String name, Class<T> objectType) {
        return getPropertyAsSet(name, objectType);
    }
    
    public static class EntityId extends JcrEntity.EntityId implements ExtensibleEntity.ID {
        
        private static final long serialVersionUID = 1L;

        public EntityId(Serializable ser) {
            super(ser);
        }
    }

}
