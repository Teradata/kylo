/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.api;

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials;

/**
 *
 */
public interface CredentialInjector {

    DataSource injectCredentials(DataSource ds);
    
    DataSource injectCredentials(DataSource ds, DataSourceCredentials creds);
}
