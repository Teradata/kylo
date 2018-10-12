/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

/*-
 * #%L
 * kylo-metadata-api
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
import java.util.Optional;

/**
 * Interface to be extended by providers that provide versionable entities, and whose
 * entities can transition into a "draft" version and later versioned again.
 */
public interface EntityDraftVersionProvider<T, PK extends Serializable> extends EntityVersionProvider<T, PK> {

    boolean hasDraftVersion(PK entityId);
    
    Optional<EntityVersion<PK, T>> findDraftVersion(PK entityId, boolean includeContent);
    
    EntityVersion<PK, T> createDraftVersion(PK entityId, boolean includeContent);
    
    EntityVersion<PK, T> createDraftVersion(PK entityId, EntityVersion.ID versionId, boolean includeContent);
    
    EntityVersion<PK, T> createVersion(PK entityId, String comment, boolean includeContent);
    
    Optional<EntityVersion<PK, T>> revertDraftVersion(PK entityId, boolean includeContent);
    
}
