package com.thinkbiganalytics.metadata.api;

/*-
 * #%L
 * thinkbig-metadata-api
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

import javax.annotation.Nonnull;

/**
 * Thrown to indicate that a require user-defined property is missing a value.
 */
public class MissingUserPropertyException extends RuntimeException {

    private static final long serialVersionUID = -8809671023051214040L;

    /**
     * Property this is missing a value
     */
    @Nonnull
    private String propertyName;

    /**
     * Constructs a {@code MissingUserPropertyException} for the specified property.
     *
     * @param propertyName the property that is missing a value
     */
    public MissingUserPropertyException(@Nonnull final String propertyName) {
        super("A required property is missing a value: " + propertyName);
        this.propertyName = propertyName;
    }

    /**
     * Gets the property that is missing a value.
     *
     * @return the name of the property
     */
    @Nonnull
    public String getPropertyName() {
        return propertyName;
    }
}
