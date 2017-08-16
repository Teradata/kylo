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
 *
 */
public interface EntityVersionProvider<T, PK extends Serializable> {
    
    EntityVersion.ID resolveVersion(Serializable ser);

    Optional<List<EntityVersion<T>>> findVersions(PK id, boolean includedContent);
    
    Optional<EntityVersion<T>> findVersion(PK entityId, EntityVersion.ID versionId, boolean includedContent);
    
    Optional<EntityVersion<T>> findLatestVersion(PK entityId, boolean includedContent);
}
