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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.catalog.credential.spi.AbstractDataSourceCredentialProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public class SimpleDataSourceCredentialProvider extends AbstractDataSourceCredentialProvider {

    private static final TypeReference<Map<String, Credentials>> CRED_MAP_TYPE = new TypeReference<Map<String, Credentials>>() { };

    /** Map of connector credentials */
    private Map<String, Credentials> credentials = new HashMap<>();

    public SimpleDataSourceCredentialProvider() {
        super();
    }
    
    public SimpleDataSourceCredentialProvider(Map<String, Credentials> creds) {
        this.credentials.putAll(creds);
    }
    
    public void loadCredentials(Resource configResource) throws IOException {
        final String connectionsJson = IOUtils.toString(configResource.getInputStream(), StandardCharsets.UTF_8);
        final Map<String, Credentials> loaded = ObjectMapperSerializer.deserialize(connectionsJson, CRED_MAP_TYPE);
        this.credentials.putAll(loaded);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#accepts(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public boolean accepts(DataSource ds) {
        String connId = ds.getConnector().getId();
        return this.credentials.containsKey(connId);
    }

    @Override
    public Void removeCredentials(DataSource ds) {
        this.credentials.remove(ds.getConnector().getId());
        return null;
    }

    protected Optional<Credentials> doGetCredentials(DataSource ds, Set<Principal> principals) {
        String id = ds.getConnector().getId();
        return Optional.ofNullable(this.credentials.get(id));
    }
}
