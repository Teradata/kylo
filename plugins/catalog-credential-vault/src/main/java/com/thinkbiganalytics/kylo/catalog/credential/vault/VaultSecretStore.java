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
import com.thinkbiganalytics.kylo.catalog.credential.spi.AbstractDataSourceCredentialProvider.CredentialEntry;
import com.thinkbiganalytics.kylo.catalog.credential.spi.AbstractDataSourceCredentialProvider.Credentials;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Data;

import static com.thinkbiganalytics.kylo.catalog.credential.vault.VaultSecretStore.CredentialType.DEFAULTS;
import static com.thinkbiganalytics.kylo.catalog.credential.vault.VaultSecretStore.CredentialType.GROUPS;
import static com.thinkbiganalytics.kylo.catalog.credential.vault.VaultSecretStore.CredentialType.USERS;

public class VaultSecretStore implements SecretStore {

    private static final TypeReference<Map<String, CredentialEntry>> CRED_OPTIONS =
        new TypeReference<Map<String, CredentialEntry>>() { };

    @Data
    public static class CredOptions {
        private String options;
        @SuppressWarnings("unused") //used during json de-serialisation
        public CredOptions() {}
        CredOptions(Map<String, CredentialEntry> options) {
            this.options = ObjectMapperSerializer.serialize(options);
        }

    }

    @Inject
    private VaultTemplate vaultTemplate;

    enum CredentialType { USERS, GROUPS, DEFAULTS}

    private final String rootPath;

    VaultSecretStore(String path) {
        if (StringUtils.isBlank(path)) {
            path = "secret/kylo/catalog/datasource/";
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        this.rootPath = path;
    }

    @Override
    public void write(String secretId, Credentials secret) {
        secret.getUserCredentials().forEach((principalName, options) -> write(secretId, USERS, principalName, options));
        secret.getGroupCredentials().forEach((principalName, options) -> write(secretId, GROUPS, principalName, options));
        write(secretId, DEFAULTS, null, secret.getDefaultCredentials());
    }

    private void write(String relativePath, CredentialType type, String principalName, Map<String, CredentialEntry> options) {
        String absPath = getAbsolutePath(relativePath, type);
        String path = getPathForPrincipalName(absPath, principalName);
        vaultTemplate.write(path, new CredOptions(options));
    }

    @Override
    public Credentials read(String secretId, Set<Principal> principals) {
        Credentials c = new Credentials();
        principals.stream().filter(UsernamePrincipal.class::isInstance).findFirst().ifPresent(principal -> {
            Map<String, CredentialEntry> options = read(secretId, USERS, principal.getName());
            if (options != null) {
                c.getUserCredentials().put(principal.getName(), options);
            }
        });

        principals.stream().filter(GroupPrincipal.class::isInstance).forEach(principal -> {
            Map<String, CredentialEntry> options = read(secretId, GROUPS, principal.getName());
            if (options != null) {
                c.getGroupCredentials().put(principal.getName(), options);
            }
        });

        Map<String, CredentialEntry> options = read(secretId, DEFAULTS, null);
        if (options != null) {
            c.getDefaultCredentials().putAll(options);
        }

        return isEmpty(c) ? null : c;
    }

    private boolean isEmpty(Credentials c) {
        return c.getDefaultCredentials().isEmpty() && c.getGroupCredentials().isEmpty() && c.getUserCredentials().isEmpty();
    }

    private Map<String, CredentialEntry> read(String relativePath, CredentialType type, String principalName) {
        String absPath = getAbsolutePath(relativePath, type);
        String path = getPathForPrincipalName(absPath, principalName);
        VaultResponseSupport<CredOptions> read = vaultTemplate.read(path, CredOptions.class);
        if (read != null) {
            CredOptions data = read.getData();
            return ObjectMapperSerializer.deserialize(data.options, CRED_OPTIONS);
        }
        return null;
    }

    @Override
    public boolean contains(String secretId) {
        List<String> list = vaultTemplate.list(rootPath);
        return list.contains(secretId + "/");
    }

    @Override
    public void remove(String secretId) {
        vaultTemplate.delete(getAbsolutePath(secretId, DEFAULTS));

        String usersPath = getAbsolutePath(secretId, USERS);
        List<String> users = vaultTemplate.list(usersPath);
        users.forEach(u -> vaultTemplate.delete(getPathForPrincipalName(usersPath, u)));

        String groupsPath = getAbsolutePath(secretId, GROUPS);
        List<String> groups = vaultTemplate.list(groupsPath);
        groups.forEach(g -> vaultTemplate.delete(getPathForPrincipalName(groupsPath, g)));
    }

    private String getPathForPrincipalName(String path, String principalName) {
        //there is no principal for default credentials
        return StringUtils.isBlank(principalName) ? path : path + "/" + principalName;
    }

    private String getAbsolutePath(String relativePath, CredentialType type) {
        return rootPath + relativePath + "/" + type.name().toLowerCase();
    }
}
