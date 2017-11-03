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

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntity;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.security.UsernamePrincipal;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrEntity extends JcrObject implements ExtensibleEntity {

    public static final String TAGGABLE_NAME = JcrPropertyConstants.TAGGABLE;

    public static final String OWNER = "jcr:createdBy";

    /**
     *
     */
    public JcrEntity(Node node) {
        super(node);
    }

    public Set<String> addTag(String tag) {
        Set<String> tags = getTags();
        if (!hasTag(tag)) {
            tags.add(tag);
            setTags(tags);
        }
        return tags;
    }

    public boolean hasTag(String tag) {
        Set<String> tags = getPropertyAsSet(TAGGABLE_NAME, String.class);
        return tags.contains(tag);
    }

    public Set<String> getTags() {
        return getPropertyAsSet(TAGGABLE_NAME, String.class);
    }

    public void setTags(Set<String> tags) {
        setProperty(TAGGABLE_NAME, tags);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.category.CustomEntity#getId()
     */
    @Override
    public ID getId() {
        try {
            return new EntityId(getObjectId());
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

    public Principal getOwner(){
        return new UsernamePrincipal(getProperty(OWNER, String.class));
    }


    public static class EntityId extends BaseId implements ID {

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
