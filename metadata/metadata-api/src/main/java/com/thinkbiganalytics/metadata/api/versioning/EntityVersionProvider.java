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

    List<EntityVersion<T>> findVersions(PK id);
    
    Optional<EntityVersion<T>> findVersion(PK entityId, EntityVersion.ID versionId);
    
    Optional<EntityVersion<T>> findLatestVersion(PK entityId);
}
