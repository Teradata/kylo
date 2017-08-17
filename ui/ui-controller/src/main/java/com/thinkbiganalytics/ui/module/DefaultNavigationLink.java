package com.thinkbiganalytics.ui.module;
/*-
 * #%L
 * kylo-ui-controller
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.ui.api.module.NavigationLink;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultNavigationLink implements NavigationLink {

     private String toggleGroupName;
     private String sref;
     private LINK_TYPE type;
     private String icon;
     private String text;
     private String narrowText;
     private boolean defaultActive;
     private boolean fullscreen;
     private boolean expanded;
     private String elementId;

    @JsonSerialize(contentAs = DefaultNavigationLink.class)
    @JsonDeserialize(contentAs = DefaultNavigationLink.class)
     private List<NavigationLink> links;

     private List<String> permission;

    @Override
    public String getToggleGroupName() {
        return toggleGroupName;
    }

    public void setToggleGroupName(String toggleGroupName) {
        this.toggleGroupName = toggleGroupName;
    }

    @Override
    public String getSref() {
        return sref;
    }

    public void setSref(String sref) {
        this.sref = sref;
    }

    @Override
    public LINK_TYPE getType() {
        return type;
    }

    public void setType(LINK_TYPE type) {
        this.type = type;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getNarrowText() {
        return narrowText;
    }

    public void setNarrowText(String narrowText) {
        this.narrowText = narrowText;
    }

    @Override
    public boolean isDefaultActive() {
        return defaultActive;
    }

    public void setDefaultActive(boolean defaultActive) {
        this.defaultActive = defaultActive;
    }

    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public List<String> getPermission() {
        return permission;
    }

    public void setPermission(List<String> permission) {
        this.permission = permission;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public List<NavigationLink> getLinks() {
        return links;
    }

    public void setLinks(List<NavigationLink> links) {
        this.links = links;
    }
}
