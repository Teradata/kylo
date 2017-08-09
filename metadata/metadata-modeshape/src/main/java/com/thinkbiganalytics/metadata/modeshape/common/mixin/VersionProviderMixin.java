/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

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
import java.util.List;
import java.util.Optional;

import com.thinkbiganalytics.metadata.api.versioning.EntityVersion;
import com.thinkbiganalytics.metadata.api.versioning.EntityVersionProvider;

/**
 *
 */
public interface VersionProviderMixin<T, PK extends Serializable> extends EntityVersionProvider<T, PK> {

    default List<EntityVersion<T>> findVersions(PK id) {
        
        return null;
    }
    
    default Optional<EntityVersion<T>> findVersion(PK entityId, EntityVersion.ID versionId) {
        
        return null;
    }

    default Optional<EntityVersion<T>> findLatestVersion(PK entityId) {
        
        return null;
    }
}
