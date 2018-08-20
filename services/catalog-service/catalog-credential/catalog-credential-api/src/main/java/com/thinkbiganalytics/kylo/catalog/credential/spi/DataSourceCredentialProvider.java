/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.spi;

/*-
 * #%L
 * kylo-catalog-credential-api
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.springframework.core.annotation.Order;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

/**
 * A service provider interface to be implemented by providers of data source authentication credentials.
 */
@Order(DataSourceCredentialProvider.DEFAULT_ORDER)
public interface DataSourceCredentialProvider {
    
    int MIN_ORDER = Integer.MAX_VALUE - 100;
    int DEFAULT_ORDER = 100;

    /**
     * Indicates whether this provider supplies credentials for a given data source.
     * @param ds the data source
     * @return true if credentials may be accessed for this data source
     */
    boolean accepts(DataSource ds);
    
    /**
     * Retrieves the credentials for the specified data source based upon the requester's
     * security principals.
     * @param ds the data source
     * @param principals the requester's principals
     * @return the credential properties applicable for the data source
     */
    Map<String, String> getCredentials(DataSource ds, Set<Principal> principals);
    
    /**
     * Removes credentials for given data source
     * @param ds the data source
     */
    Void removeCredentials(DataSource ds);

    /**
     * Creates/modifies the data source properties by inserting placeholders for the data
     * source's credentials based upon configured authentication scheme.
     * @param ds the data source
     * @param principals the requester's principals
     * @return a modified data source containing the placeholders
     */
    DataSource applyPlaceholders(DataSource ds, Set<Principal> principals);
    
    /**
     * Modifies the specified data source by embedding the appropriate credentials
     * into its properties based upon configured authentication scheme.
     * @param ds the data source
     * @param principals the requester's principals
     * @return a modified data source containing authentication credentials
     */
    DataSource applyCredentials(DataSource ds, Set<Principal> principals);
}
