/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.plugin;

/*-
 * #%L
 * kylo-catalog-core
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

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 */
public class DefaultConnectorPluginManager implements ConnectorPluginManager {
    
    private final Set<ConnectorPlugin> plugins = Collections.synchronizedSet(new HashSet<>());

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager#getPlugins()
     */
    @Override
    public List<ConnectorPlugin> getPlugins() {
        Stream<ConnectorPlugin> stream = this.plugins.stream();  // Sort?
        
        synchronized (this.plugins) {
            return stream.collect(Collectors.toList());
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager#getPlugin(java.lang.String)
     */
    @Override
    public Optional<ConnectorPlugin> getPlugin(String pluginId) {
        Stream<ConnectorPlugin> stream = this.plugins.stream().filter(p -> p.getId().equals(pluginId));
        
        synchronized (this.plugins) {
            return stream.findFirst();
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager#register(com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin)
     */
    @Override
    public void register(ConnectorPlugin plugin) {
        this.plugins.add(plugin);
    }

}
