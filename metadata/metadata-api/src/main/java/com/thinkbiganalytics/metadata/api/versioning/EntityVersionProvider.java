/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface EntityVersionProvider<T, PK extends Serializable> {

    Optional<List<EntityVersion<T>>> findVersions(PK id, boolean includeEntity);
    
    Optional<EntityVersion<T>> findVersion(PK entityId, EntityVersion.ID versionId, boolean includeEntity);
    
    Optional<EntityVersion<T>> findLatestVersion(PK entityId, boolean includeEntity);
}
