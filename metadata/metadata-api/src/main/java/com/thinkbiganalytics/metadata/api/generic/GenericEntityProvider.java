/**
 *
 */
package com.thinkbiganalytics.metadata.api.generic;

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
public interface GenericEntityProvider {

    GenericType createType(String name, Map<String, GenericType.PropertyType> props);

    GenericType createType(String name, GenericEntity supertype, Map<String, GenericType.PropertyType> props);

    GenericType getType(String name);

    List<GenericType> getTypes();

//    List<GenericType> getTypes(GenericType type);

    GenericEntity createEntity(GenericType type, Map<String, Object> props);

    GenericEntity getEntity(GenericEntity.ID id);

    List<GenericEntity> getEntities();


}
