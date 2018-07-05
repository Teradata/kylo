/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import java.util.Optional;

/**
 *
 */
public interface ConnectorProvider extends BaseProvider<Connector, Connector.ID> {
    
    Connector create(String pluginId);
    
    Optional<Connector> findByPluginId(String pluginId);
    
    
}
