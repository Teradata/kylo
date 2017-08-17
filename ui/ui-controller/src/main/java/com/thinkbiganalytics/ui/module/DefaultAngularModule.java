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
import com.thinkbiganalytics.ui.api.module.AngularModule;
import com.thinkbiganalytics.ui.api.module.AngularStateMetadata;
import com.thinkbiganalytics.ui.api.module.NavigationLink;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultAngularModule implements AngularModule {

    @JsonSerialize(contentAs = DefaultAngularStateMetadata.class)
    @JsonDeserialize(contentAs = DefaultAngularStateMetadata.class)
    private List<AngularStateMetadata> states;

    private String moduleJsUrl;

    @JsonSerialize(contentAs = DefaultNavigationLink.class)
    @JsonDeserialize(contentAs = DefaultNavigationLink.class)
    private List<NavigationLink> navigation;


    public DefaultAngularModule(){

    }

    public List<AngularStateMetadata> getStates() {
        return states;
    }

    public void setStates(List<AngularStateMetadata> states) {
        this.states = states;
    }


    @Override
    public String getModuleJsUrl() {
        return moduleJsUrl;
    }

    public void setModuleJsUrl(String moduleJsUrl) {
        this.moduleJsUrl = moduleJsUrl;
    }

    public List<NavigationLink> getNavigation() {
        return navigation;
    }

    public void setNavigation(List<NavigationLink> navigation) {
        this.navigation = navigation;
    }
}
