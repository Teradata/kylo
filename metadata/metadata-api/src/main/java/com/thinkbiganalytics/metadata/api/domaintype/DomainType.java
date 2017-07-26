package com.thinkbiganalytics.metadata.api.domaintype;

/*-
 * #%L
 * kylo-metadata-api
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

import java.io.Serializable;

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
public interface DomainType extends Serializable {

    /**
     * Gets the unique identifier.
     */
    ID getId();

    /**
     * Gets a human-readable description.
     */
    String getDescription();

    /**
     * Sets a human-readable description.
     */
    void setDescription(String value);

    /**
     * Gets the field metadata as a JSON document.
     */
    String getFieldJson();

    /**
     * Sets the field metadata as a JSON document.
     */
    void setFieldJson(String value);

    /**
     * Gets the field policy as a JSON document.
     */
    String getFieldPolicyJson();

    /**
     * Sets the field policy as a JSON document.
     */
    void setFieldPolicyJson(String value);

    /**
     * Gets the name of the icon.
     */
    String getIcon();

    /**
     * Sets the name of the icon.
     */
    void setIcon(String value);

    /**
     * Gets the icon color.
     */
    String getIconColor();

    /**
     * Sets the icon color.
     */
    void setIconColor(String value);

    /**
     * Gets the flags for the regular expression.
     */
    String getRegexFlags();

    /**
     * Sets the flags for the regular expression.
     */
    void setRegexFlags(String value);

    /**
     * Gets the regular expression for matching sample data.
     */
    String getRegexPattern();

    /**
     * Sets the regular expression for matching sample data.
     */
    void setRegexPattern(String value);

    /**
     * Gets a human-readable title.
     */
    String getTitle();

    /**
     * Sets a human-readable title.
     */
    void setTitle(String value);

    /**
     * A unique identifier for a domain type.
     */
    interface ID extends Serializable {

    }
}
