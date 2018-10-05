/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

/*-
 * #%L
 * kylo-metadata-api
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

import com.thinkbiganalytics.metadata.api.EntityAlreadyExistsException;

/**
 * Thrown when a attempt to create a connector fails because one already exists with
 * the same system name.
 */
public class ConnectorAlreadyExistsException extends EntityAlreadyExistsException {

    private static final long serialVersionUID = 1L;
    
    /**
     * @param systemName
     * @return the exception
     */
    public static ConnectorAlreadyExistsException fromSystemName(String systemName) {
        return new ConnectorAlreadyExistsException("A connector already exists with the name: " + systemName, systemName);
    }
    
    public ConnectorAlreadyExistsException(String message, String systemName) {
        super(message, systemName);
    }

}
