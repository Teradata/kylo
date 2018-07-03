package com.thinkbiganalytics.kylo.catalog.credential.vault;

/*-
 * #%L
 * kylo-catalog-credential-vault
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

import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;

public class VaultSecretStore implements SecretStore {

    private static final String ROOT = "secret/kylo/catalog/";
    private static final TypeReference<Map<String, Map<String, AbstractDataSourceCredentialProvider.CredentialEntry>>> CRED_MAP_TYPE =
        new TypeReference<Map<String, Map<String, AbstractDataSourceCredentialProvider.CredentialEntry>>>() { };

    @Data
    public static class Creds {
        private String users;
        private String groups;

        @SuppressWarnings("unused")
        public Creds() {}
        Creds(AbstractDataSourceCredentialProvider.Credentials credentials) {
            this.users = ObjectMapperSerializer.serialize(credentials.getUserCredentials());
            this.groups = ObjectMapperSerializer.serialize(credentials.getGroupCredentials());
        }
    }

    @Inject
    private VaultTemplate vaultTemplate;

    @Override
    public void write(String path, AbstractDataSourceCredentialProvider.Credentials secret) {
        Creds c = new Creds(secret);
        vaultTemplate.write(ROOT + path, c);
    }

    @Override
    public AbstractDataSourceCredentialProvider.Credentials read(String path) {
        VaultResponseSupport<Creds> response = vaultTemplate.read(ROOT + path, Creds.class);
        if (response != null) {
            Creds c = response.getData();
            AbstractDataSourceCredentialProvider.Credentials credentials = new AbstractDataSourceCredentialProvider.Credentials();
            Map<String, Map<String, AbstractDataSourceCredentialProvider.CredentialEntry>> users = ObjectMapperSerializer.deserialize(c.users, CRED_MAP_TYPE);
            credentials.setUserCredentials(users);
            Map<String, Map<String, AbstractDataSourceCredentialProvider.CredentialEntry>> groups = ObjectMapperSerializer.deserialize(c.groups, CRED_MAP_TYPE);
            credentials.setGroupCredentials(groups);
            return credentials;
        }
        return null;
    }

    @Override
    public boolean contains(String path) {
        List<String> list = vaultTemplate.list(ROOT);
        return list.contains(path);
    }

    @Override
    public void remove(String path) {
        vaultTemplate.delete(ROOT + path);
    }
}
