/**
 *
 */
package com.thinkbiganalytics.kylo.catalog.credential.vault;

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

import com.thinkbiganalytics.kylo.catalog.credential.spi.AbstractDataSourceCredentialProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

/**
 * Credential provider which stores credentials in Vault
 */
public class VaultDataSourceCredentialProvider extends AbstractDataSourceCredentialProvider {

    @Inject
    private SecretStore secretStore;

    @Override
    public boolean accepts(DataSource ds) {
        return true;
    }

    @Override
    public Void removeCredentials(DataSource ds) {
        secretStore.remove(ds.getId());
        return null;
    }

    @Override
    protected Optional<Credentials> doGetCredentials(DataSource ds, Set<Principal> principals) {
        return Optional.ofNullable(secretStore.read(ds.getId(), principals));
    }
}
