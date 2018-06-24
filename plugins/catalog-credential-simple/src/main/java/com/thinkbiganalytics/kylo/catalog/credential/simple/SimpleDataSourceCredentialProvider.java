/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.simple;

/*-
 * #%L
 * kylo-catalog-credential-simple
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

import com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SimpleDataSourceCredentialProvider implements DataSourceCredentialProvider {

    /**
     * 
     */
    public SimpleDataSourceCredentialProvider() {
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#accepts(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public boolean accepts(DataSource ds) {
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public DataSource applyCredentials(DataSource ds, Set<Principal> principals) {
        // TODO Implement this
        return ds;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyPlaceholders(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
     */
    @Override
    public DataSource applyPlaceholders(DataSource ds, Set<Principal> principals) {
        // TODO Implement this
        return ds;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#getCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public Map<String, String> getCredentials(DataSource ds, Set<Principal> principals) {
        // TODO Implement this
        Map<String, String> creds = new HashMap<>();
        creds.put("password", "ThisIsAPassword");
        return creds;
    }

}
