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
import java.util.List;
import java.util.Optional;

/**
 * Interface to be extended by providers that provide versionable entities.
 */
public interface EntityVersionProvider<T, PK extends Serializable> {
    
    /**
     * @param ser some serialized form of the version ID
     * @return a version ID instance
     */
    EntityVersion.ID resolveVersion(Serializable ser);

    /**
     * Find all versions of an entity.
     * @param id the ID of the entity
     * @param includedContent true if the entities should be included in the versions returned
     * @return An optional list of versions, empty if the entity does not exist
     * @throws VersionableEntityNotFound if an entity with the ID does not exist
     */
    List<EntityVersion<PK, T>> findVersions(PK id, boolean includeContent);
    
    /**
     * Find a particular version of an entity.
     * @param id the ID of the entity
     * @param includedContent true if the entity should be included in the version returned
     * @return An optional version of the entity, empty if the version does not exist
     */
    Optional<EntityVersion<PK, T>> findVersion(PK entityId, EntityVersion.ID versionId, boolean includeContent);
    
    /**
     * Find the most recent version of an entity.
     * @param id the ID of the entity
     * @param includedContent true if the entity should be included in the version returned
     * @return An optional latest version of the entity, empty if a version does not exist
     */
    Optional<EntityVersion<PK, T>> findLatestVersion(PK entityId, boolean includeContent);
//    
//    /**
//     * Removes a specific version of an entity and returns the prior version if it exists.
//     * @param id the ID of the entity
//     * @param includedContent true if the entity should be included in the version returned
//     * @return An optional earlier version of the entity, empty if no earlier version exists
//     */
//    Optional<EntityVersion<PK, T>> removeVersion(PK entityId, EntityVersion.ID versionId, boolean includeContent);
}
