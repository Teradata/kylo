/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleEntityProvider {

    //    List<ExtensibleType> getTypes(ExtensibleType type);
    
    ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props);

    List<ExtensibleEntity> getEntities();

    ExtensibleEntity getEntity(ExtensibleEntity.ID id);

}
