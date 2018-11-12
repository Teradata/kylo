/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.common;

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

import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.TaggableMixin;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
//public abstract class JcrEntity<I extends Serializable> extends JcrObject implements ExtensibleEntity<I> {
public abstract class JcrEntity<I extends Serializable> extends JcrObject implements TaggableMixin {

    public static final String OWNER = "tba:owner";
    public static final String DEFAULT_OWNER = "jcr:createdBy";

    /**
     *
     */
    public JcrEntity(Node node) {
        super(node);
    }
    
    public abstract I getId();

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getTypeName()
     */
    @Override
    public String getTypeName() {
        try {
            return getNode().getPrimaryNodeType().getName().replace(JcrMetadataAccess.TBA_PREFIX + ":", "");
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity type name", e);
        }
    }

    public Principal getOwner(){
        String name = getProperty(OWNER, null);
        name = name == null ? getProperty(DEFAULT_OWNER, null) : name;
        return name != null ? new UsernamePrincipal(name) : null;
    }


    public static class EntityId extends BaseId {

        private static final long serialVersionUID = -9084653006891727475L;

        private String idValue;


        public EntityId() {
        }

        public EntityId(Serializable ser) {
            super(ser);
        }

        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }

        @Override
        public UUID getUuid() {
            return UUID.fromString(idValue);
        }

        @Override
        public void setUuid(UUID uuid) {
            this.idValue = uuid.toString();

        }
    }

}
