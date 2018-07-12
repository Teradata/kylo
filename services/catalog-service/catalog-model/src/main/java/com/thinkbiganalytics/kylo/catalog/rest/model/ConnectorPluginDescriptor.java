/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Describes a connector for rendering it in a UI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectorPluginDescriptor {
    
    /**
     * Connector plugin ID (connector type)
     */
    private String pluginId;
    
    /**
     * UI plugin for creating new data sources
     */
    private UiPlugin dataSourcePlugin;

    /**
     * UI tab plugins for setting data set properties
     */
    private List<ConnectorTab> tabs;
 
    /**
     * A list of options which describe how UI should be displayed for each option
     * a data source would need
     */
    private List<UiOption> options;

    /**
     * Optional identifier for an object which knows how to map Connector UiOptions to Datasource options
     */
    private String optionsMapperId;
    
    /**
     * 
     */
    public ConnectorPluginDescriptor() {
    }
    
    /**
     * 
     */
    public ConnectorPluginDescriptor(@Nonnull final ConnectorPluginDescriptor other) {
        this.pluginId = other.pluginId;
        this.dataSourcePlugin = (other.dataSourcePlugin != null) ? new UiPlugin(other.dataSourcePlugin) : null;
        this.optionsMapperId = other.optionsMapperId;
        
        if (other.tabs != null) {
            List<ConnectorTab> tabs = new ArrayList<>();
            for(ConnectorTab tab: other.tabs){
                tabs.add(new ConnectorTab(tab));
            }
            this.tabs = tabs;
        }
        
        if (other.options != null) {
            List<UiOption> options = new ArrayList<>();
            for(UiOption o: other.options){
                options.add(new UiOption(o));
            }
            this.options = options;
        }
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public UiPlugin getDataSourcePlugin() {
        return dataSourcePlugin;
    }

    public void setDataSourcePlugin(UiPlugin dataSourcePlugin) {
        this.dataSourcePlugin = dataSourcePlugin;
    }

    public List<ConnectorTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<ConnectorTab> tabs) {
        this.tabs = tabs;
    }

    public List<UiOption> getOptions() {
        return options;
    }

    public void setOptions(List<UiOption> options) {
        this.options = options;
    }

    public String getOptionsMapperId() {
        return optionsMapperId;
    }

    public void setOptionsMapperId(String optionsMapperId) {
        this.optionsMapperId = optionsMapperId;
    }

    
}
