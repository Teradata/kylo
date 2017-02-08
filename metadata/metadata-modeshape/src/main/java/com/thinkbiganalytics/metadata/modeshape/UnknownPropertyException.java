/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

/**
 *
 */
public class UnknownPropertyException extends RuntimeException {

    private static final long serialVersionUID = 4878659919254883237L;

    private final String propertyName;


    /**
     * @param message
     */
    public UnknownPropertyException(String propName) {
        super("Unknown property: " + propName);
        this.propertyName = propName;
    }

    /**
     * @param propName
     * @param cause
     */
    public UnknownPropertyException(String propName, Throwable cause) {
        super("Unknown property: " + propName, cause);
        this.propertyName = propName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
