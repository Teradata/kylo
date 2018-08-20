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
import com.thinkbiganalytics.kylo.catalog.credential.spi.CredentialProviderException;
import com.thinkbiganalytics.kylo.catalog.credential.spi.CredentialProviderNotFoundExeption;
import com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * The default implementation of DataSourceCredentialManager that delegates to a chain
 * of one or more {@link DataSourceCredentialProvider}s to apply or retrieve credentials
 * to their supported {@link DataSource}s.
 */
public class DefaultDataSourceCredentialManager implements DataSourceCredentialManager {
    
    @Inject
    private EncryptionService encryptionService;
    
    private final List<DataSourceCredentialProvider> providers;
    
    public DefaultDataSourceCredentialManager(List<DataSourceCredentialProvider> providers) {
        super();
        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager#applyCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public DataSource applyCredentials(DataSource dataSource, Set<Principal> principals) {
        return applyToProvider((provider,  ds) -> provider.applyCredentials(ds, principals), dataSource);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager#applyPlaceholders(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
     */
    @Override
    public DataSource applyPlaceholders(DataSource dataSource, Set<Principal> principals) {
        return applyToProvider((provider,  ds) -> provider.applyPlaceholders(ds, principals), dataSource);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager#getCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public Map<String, String> getCredentials(DataSource dataSource, boolean encrypted, Set<Principal> principals) {
        Map<String, String> creds = applyToProvider((provider,  ds) -> provider.getCredentials(ds, principals), dataSource);
        return encrypted ? encrypt(creds) : creds;
    }

    @Override
    public void removeCredentials(DataSource dataSource) {
        applyToProvider(DataSourceCredentialProvider::removeCredentials, dataSource);
    }

    /**
     * @param creds unencrypted credentials
     * @return a new properties with the values encrypted
     */
    protected Map<String, String> encrypt(Map<String, String> creds) {
        return creds.entrySet().stream()
                .filter(entry -> ! this.encryptionService.isEncrypted(entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, entry -> this.encryptionService.encrypt(entry.getValue())));
    }

    protected <R> R applyToProvider(BiFunction<DataSourceCredentialProvider, DataSource, R> func, DataSource ds) {
        Exception firstX = null;

        List<DataSourceCredentialProvider> acceptingProviders = findAcceptingProviders(ds);
        for (DataSourceCredentialProvider provider : acceptingProviders) {
            try {
                return func.apply(provider, ds);
            } catch (Exception e) {
                firstX = firstX == null ? e :firstX;
            }
        }
        
        if (firstX == null) {
            throw new CredentialProviderNotFoundExeption(ds);
        } else {
            throw new CredentialProviderException(ds, "Failed to apply credentials to DataSource: " + ds, firstX);
        }
    }
    
    protected List<DataSourceCredentialProvider> findAcceptingProviders(final DataSource ds) {
        return this.providers.stream().filter(p -> p.accepts(ds)).collect(Collectors.toList());
    }
}
