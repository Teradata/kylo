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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * A type of data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("unused")
public class Connector {

    /**
     * Color of the icon
     */
    private String color;

    /**
     * UI plugin for creating new data sources
     */
    private UiPlugin dataSourcePlugin;

    /**
     * Name of the icon
     */
    private String icon;

    /**
     * Unique identifier
     */
    private String id;

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
     * Properties to apply to all data sets
     */
    @JsonDeserialize(as = DefaultDataSetTemplate.class)
    @JsonSerialize(as = DefaultDataSetTemplate.class)
    private DataSetTemplate template;

    /**
     * Display name of this connector
     */
    private String title;

    public Connector() {
    }

    public Connector(@Nonnull final Connector other) {
        color = other.color;
        dataSourcePlugin = (other.dataSourcePlugin != null) ? new UiPlugin(other.dataSourcePlugin) : null;
        icon = other.icon;
        id = other.id;
        if(other.tabs != null){
            List<ConnectorTab> tabs = new ArrayList<>();
            for(ConnectorTab tab: other.tabs){
                tabs.add(new ConnectorTab(tab));
            }
            this.tabs = tabs;
        }
        if(other.options != null){
            List<UiOption> options = new ArrayList<>();
            for(UiOption o: other.options){
                options.add(new UiOption(o));
            }
            this.options = options;
        }

        template = (other.template != null) ? new DefaultDataSetTemplate(other.template) : null;
        title = other.title;
        optionsMapperId = other.optionsMapperId;
    }

    public String getOptionsMapperId() {
        return optionsMapperId;
    }

    public void setOptionsMapperId(String optionsMapperId) {
        this.optionsMapperId = optionsMapperId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public UiPlugin getDataSourcePlugin() {
        return dataSourcePlugin;
    }

    public void setDataSourcePlugin(UiPlugin dataSourcePlugin) {
        this.dataSourcePlugin = dataSourcePlugin;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public DataSetTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DataSetTemplate template) {
        this.template = template;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Connector{id=" + id + ", title='" + title + "'}";
    }
}
