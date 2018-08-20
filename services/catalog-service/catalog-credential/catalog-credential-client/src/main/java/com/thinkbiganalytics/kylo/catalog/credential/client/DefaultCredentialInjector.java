/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.client;

/*-
 * #%L
 * kylo-catalog-credential-client
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

import com.thinkbiganalytics.kylo.catalog.credential.api.CredentialInjector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

import java.util.Map;

import javax.inject.Inject;

/**
 *
 */
public class DefaultCredentialInjector implements CredentialInjector {

    @Inject
    public EncryptionService encryptionService;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.api.CredentialInjector#injectCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public DataSource injectCredentials(DataSource ds) {
        if (ds.hasCredentials()) {
            return injectCredentials(ds, ds.getCredentials());
        } else {
            throw new DataSourceCredentialsException("No credentials present in the supplied data source: " + ds.getTitle());
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.api.CredentialInjector#injectCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials)
     */
    @Override
    public DataSource injectCredentials(DataSource ds, DataSourceCredentials creds) {
        DataSource injected = new DataSource(ds); 
        Map<String, String> props = injected.getCredentials().getProperties();
        
        try {
            if (creds.isEncrypted()) {
                props.entrySet().forEach(entry -> entry.setValue(this.encryptionService.decrypt(entry.getValue())));
            }
            
            return injected;
        } catch (Exception e) {
            throw new DataSourceCredentialsException("Unable to inject the credentials into the supplied data source: " + ds.getTitle(), e);
        }
    }
    
    
}
