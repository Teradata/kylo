package com.thinkbiganalytics.rest.model;

/*-
 * #%L
 * thinkbig-commons-rest-model
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

import java.util.Map;

/**
 * supply a label, value and optional hint describing the object.
 * This is used in the Annotation framework to display data to the User interface and describe various options
 */
public class LabelValue {

    /**
     * the display name
     */
    private String label;
    /**
     * the actual value
     */
    private String value;

    /**
     * a hint describing in more detail about this option
     */
    private String hint;

    /**
     * Additional properties
     */
    private Map<String,Object> properties;

    public LabelValue() {

    }

    public LabelValue(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public LabelValue(String label, String value, String hint) {
        this.label = label;
        this.value = value;
        this.hint = hint;
    }

    public LabelValue(String label, String value, String hint, Map<String,Object>properties) {
        this.label = label;
        this.value = value;
        this.hint = hint;
        this.properties = properties;
    }



    /**
     * get the display label
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * set the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * get additional information describing this option
     */
    public String getHint() {
        return hint;
    }

    /**
     * set additional information describing this option
     */
    public void setHint(String hint) {
        this.hint = hint;
    }


    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
