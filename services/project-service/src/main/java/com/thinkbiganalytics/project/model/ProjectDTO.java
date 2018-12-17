package com.thinkbiganalytics.project.model;

/*-
 * #%L
 * project-service
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
import com.thinkbiganalytics.metadata.api.project.Project;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDTO {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String iconColor;

    public ProjectDTO() {}

    public ProjectDTO(Project domain) {
        this.id = domain.getId().toString();
        this.name = domain.getName();
        this.description = domain.getDescription();
        this.icon = domain.getIcon();
        this.iconColor = domain.getIconColor();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public static Project toDomain(ProjectDTO dto, Project domain) {

        domain.setName(dto.getName());
        domain.setDescription(dto.getDescription());
        domain.setIcon(dto.getIcon());
        domain.setIconColor(dto.getIconColor());
        return domain;
    }
}
