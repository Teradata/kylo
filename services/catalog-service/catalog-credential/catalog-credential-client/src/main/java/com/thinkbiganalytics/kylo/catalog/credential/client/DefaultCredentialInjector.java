/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.client;

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
