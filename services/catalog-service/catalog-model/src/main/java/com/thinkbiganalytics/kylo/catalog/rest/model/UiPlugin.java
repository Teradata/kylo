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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Reference to a Kylo UI plugin.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiPlugin {

    /**
     * Reference to a Ui-Router state
     */
    private String sref;

    /**
     * Dynamic UI-Router state to load
     *
     * @see <a href="https://ui-router.github.io/ng2/docs/latest/interfaces/state.statedeclaration.html" target="_blank">StateDeclaration</a>
     */
    private Map<String, Object> state;

    public UiPlugin() {
    }

    public UiPlugin(@Nonnull final UiPlugin other) {
        sref = other.sref;
        state = (other.state != null) ? new HashMap<>(other.state) : null;
    }

    public String getSref() {
        return sref;
    }

    public void setSref(String sref) {
        this.sref = sref;
    }

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }
}
