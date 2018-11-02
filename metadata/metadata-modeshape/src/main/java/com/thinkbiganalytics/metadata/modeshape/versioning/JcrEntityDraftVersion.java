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

import javax.jcr.Node;

import org.joda.time.DateTime;

import java.util.Optional;

import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

/**
 *
 */
public class JcrEntityDraftVersion<I, E> extends JcrEntityVersion<I, E> {

    private DateTime created;
    
    public static boolean matchesId(Node versionable, EntityVersion.ID id) {
        return new VersionId(JcrPropertyUtil.getIdentifier(versionable)).equals(id);
    }
    
    public JcrEntityDraftVersion(Node versionable, I entityId, E entity) {
        super(null, Optional.empty(), entityId, entity);
        setId(new VersionId(JcrPropertyUtil.getIdentifier(versionable)));
        this.created = JcrPropertyUtil.getProperty(versionable, "jcr:lastModified");
    }

    @Override
    public String getName() {
        return EntityVersion.DRAFT_NAME;
    }

    @Override
    public DateTime getCreatedDate() {
        return this.created;
    }

    
}
