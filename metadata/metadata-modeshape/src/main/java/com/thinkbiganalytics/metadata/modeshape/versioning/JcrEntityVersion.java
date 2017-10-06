/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.versioning;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.jcr.version.Version;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.core.BaseId;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

/**
 *
 */
public class JcrEntityVersion<E> implements EntityVersion<E> {

    private Version version;
    private VersionId id;
    private Optional<E> entity;
    
    public JcrEntityVersion(Version version) {
        this(version, null);
    }
    
    public JcrEntityVersion(Version version, E entity) {
        if (version != null) {
            this.version = version;
            this.id = new VersionId(JcrPropertyUtil.getIdentifier(JcrUtil.getNode(version, "jcr:frozenNode")));
//        this.id = new VersionId(JcrPropertyUtil.getIdentifier(version));
        }
        this.entity = Optional.ofNullable(entity);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.versioning.EntityVersion#getId()
     */
    @Override
    public EntityVersion.ID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.versioning.EntityVersion#getName()
     */
    @Override
    public String getName() {
        return JcrPropertyUtil.getName(this.version);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.versioning.EntityVersion#getCreatedDate()
     */
    @Override
    public DateTime getCreatedDate() {
        return JcrPropertyUtil.getProperty(this.version, "jcr:created");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.versioning.EntityVersion#getEntity()
     */
    @Override
    public Optional<E> getEntity() {
        return this.entity;
    }

    protected void setId(VersionId id) {
        this.id = id;
    }
    
    public static class VersionId implements ID {

        private static final long serialVersionUID = 1L;

        private String idValue;


        public VersionId() {
        }

        public VersionId(Serializable ser) {
            if (ser instanceof VersionId) {
                this.idValue = ((VersionId) ser).idValue;
            } else if (ser instanceof String) {
                this.idValue = (String) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }

        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                VersionId that = (VersionId) obj;
                return Objects.equals(this.idValue, that.idValue);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.idValue);
        }

    }

}
