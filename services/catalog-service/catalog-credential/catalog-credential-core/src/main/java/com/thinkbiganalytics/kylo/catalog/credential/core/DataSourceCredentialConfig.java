/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.core;

/*-
 * #%L
 * kylo-catalog-credential-core
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

import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Configuration
public class DataSourceCredentialConfig {

    @Bean
    public DataSourceCredentialManager defaultDataSourceCredentialManager(List<DataSourceCredentialProvider> providers) {
        return new DefaultDataSourceCredentialManager(providers);
    }
    
    @Bean
    public DataSourceCredentialProvider defaultDataSourceCredentialProvider() {
        return new DefaultDataSourceCredentialProvider();
    }
    
    /**
     * This implementation does not modify data sources or produce credentials.  It simply logs that is was 
     * called and returns data sources unmodified.  
     */
    @Order(DataSourceCredentialProvider.MIN_ORDER - 1)  // Should be the last provider in the chain.
    private static class DefaultDataSourceCredentialProvider implements DataSourceCredentialProvider {
        
        private static final Logger log = LoggerFactory.getLogger(DefaultDataSourceCredentialProvider.class);

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#accepts(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
         */
        @Override
        public boolean accepts(DataSource ds) {
            // Accepts all data sources as only log messages will be generated.
            return true;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#getCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
         */
        @Override
        public Map<String, String> getCredentials(DataSource ds, Set<Principal> principals) {
            log.debug("No supporting credential provider configured for data source: {}", ds.getTitle());
            return Collections.emptyMap();
        }

        @Override
        public Void removeCredentials(DataSource ds) {
            return null;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyPlaceholders(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
         */
        @Override
        public DataSource applyPlaceholders(DataSource ds, Set<Principal> principals) {
            log.debug("No supporting credential provider configured for data source: {}", ds.getTitle());
            return ds;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
         */
        @Override
        public DataSource applyCredentials(DataSource ds, Set<Principal> principals) {
            log.debug("No supporting credential provider configured for data source: {}", ds.getTitle());
            return ds;
        }
        
    }
}
