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
import com.thinkbiganalytics.ui.api.module.AngularStateMetadata;

import java.util.Map;

/**
 * Created by sr186054 on 8/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultAngularStateMetadata implements AngularStateMetadata {
    private String state;
    private String url;
    private Map<String,Object> params;

    public DefaultAngularStateMetadata(){

    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
