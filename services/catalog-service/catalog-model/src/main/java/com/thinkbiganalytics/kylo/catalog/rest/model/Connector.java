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
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;

import javax.annotation.Nonnull;

/**
 * A type of data source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Connector extends EntityAccessControl {

    /**
     * Unique identifier
     */
    private String id;

    /**
     * Connector plugin ID (connector type)
     */
    private String pluginId;

    /**
     * Display name of this connector
     */
    private String title;

    /**
     * Display name of this connector
     */
    private String description;

    /**
     * Properties to apply to all data sets
     */
    @JsonDeserialize(as = DefaultDataSetTemplate.class)
    @JsonSerialize(as = DefaultDataSetTemplate.class)
    private DataSetTemplate template;

    /**
     * Color of the icon
     */
    private String color;

    /**
     * Name of the icon
     */
    private String icon;

    public Connector() {
    }

    public Connector(@Nonnull final Connector other) {
        color = other.color;
        icon = other.icon;
        id = other.id;
        pluginId = other.pluginId;

        template = (other.template != null) ? new DefaultDataSetTemplate(other.template) : null;
        title = other.title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Connector{id=" + id + ", title='" + title + "'}";
    }
}
