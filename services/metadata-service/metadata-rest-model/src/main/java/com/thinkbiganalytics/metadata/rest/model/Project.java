package com.thinkbiganalytics.metadata.rest.model;

/*-
 * #%L
 * kylo-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;

/**
 * REST model object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends EntityAccessControl {
    private String id;
    private String systemName;
    private String projectName;
    private String description;
    private String containerImage;
    private String icon;
    private String iconColor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String image) {
        this.containerImage = image;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        if (getProjectName() != null ? !getProjectName().equals(project.getProjectName()) : project.getProjectName() != null) {
            return false;
        }
        if (getSystemName() != null ? !getSystemName().equals(project.getSystemName()) : project.getSystemName() != null) {
            return false;
        }
        if (getDescription() != null ? !getDescription().equals(project.getDescription()) : project.getDescription() != null) {
            return false;
        }
        if (getContainerImage() != null ? !getContainerImage().equals(project.getContainerImage()) : project.getContainerImage() != null) {
            return false;
        }
        if (getIcon() != null ? !getIcon().equals(project.getIcon()) : project.getIcon() != null) {
            return false;
        }
        if (getIconColor() != null ? !getIconColor().equals(project.getIconColor()) : project.getIconColor() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getProjectName() != null ? getProjectName().hashCode() : 0;
        result = 31 * result + (getSystemName() != null ? getSystemName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getContainerImage() != null ? getContainerImage().hashCode() : 0);
        result = 31 * result + (getIcon() != null ? getIcon().hashCode() : 0);
        result = 31 * result + (getIconColor() != null ? getIconColor().hashCode() : 0);
        return result;
    }
}
