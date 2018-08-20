/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.spi;

/*-
 * #%L
 * kylo-catalog-api
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

import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;

/**
 *
 */
public interface ConnectorPlugin {
    
    default String getId() {
        return getClass().getSimpleName();
    }
    
    String getVersion();

    ConnectorPluginDescriptor getDescriptor();
    
    // TODO: Would we benefit from a connector factory concept, or at least
    // a means by  which a plugin can participate in connector creation, so that
    // the plugin can get a crack at connector-specific initialization and/or
    // validation before handing the connector off to Kylo for storage in
    // the metadata store?
}
