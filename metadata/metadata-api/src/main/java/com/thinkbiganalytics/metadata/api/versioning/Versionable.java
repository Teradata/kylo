/**
 * 
 */
package com.thinkbiganalytics.metadata.api.versioning;

import java.util.List;

/**
 *
 */
public interface Versionable<E> {

    List<EntityVersion<E>> getVersions();
}
