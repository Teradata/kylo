/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/*-
 * #%L
 * kylo-metadata-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import java.util.Optional;

/**
 *
 */
//public interface ConnectorProvider extends BaseProvider<Connector, Connector.ID> {
public interface ConnectorProvider {
    
    Connector.ID resolveId(Serializable id);
    
    Connector create(String pluginId, String title);
    
    List<Connector> findAll();
    
    List<Connector> findAll(boolean includeInactive);
    
    Page<Connector> findPage(Pageable page, String filter);
    
    Optional<Connector> find(Connector.ID id);
    
    Optional<Connector> findByPlugin(String pluginId);
    
    void deleteById(Connector.ID id);
}
