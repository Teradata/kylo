/**
 *
 */
package com.thinkbiganalytics.metadata.api.extension;

/*-
 * #%L
 * thinkbig-metadata-api
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

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ExtensibleEntityProvider {

    //    List<ExtensibleType> getTypes(ExtensibleType type);

    ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props);

    ExtensibleEntity updateEntity(ExtensibleEntity extensibleEntity, Map<String, Object> props);
    
    boolean deleteEntity(ExtensibleEntity.ID id);

    List<ExtensibleEntity> getEntities();

    List<ExtensibleEntity> getEntities(String typeName);

    List<? extends ExtensibleEntity> findEntitiesMatchingProperty(String typeName, String propName, Object value);

    ExtensibleEntity getEntity(ExtensibleEntity.ID id);

}
