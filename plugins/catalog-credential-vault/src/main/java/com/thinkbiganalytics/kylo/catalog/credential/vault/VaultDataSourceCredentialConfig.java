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

import com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.ClientCertificateAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.config.ClientHttpRequestFactoryFactory;
import org.springframework.vault.support.SslConfiguration;

import java.io.File;

import javax.inject.Inject;

@Configuration
@PropertySource(value = "classpath:vault.properties", ignoreResourceNotFound = true)
@ComponentScan
@EnableConfigurationProperties
public class VaultDataSourceCredentialConfig extends AbstractVaultConfiguration {

    @Inject
    private VaultConfiguration vaultCfg;

    @Bean(name = "vaultConfiguration")
    @ConfigurationProperties(prefix = "vault")
    public VaultConfiguration vaultConfiguration() {
        return new VaultConfiguration();
    }

    @Bean
    public SecretStore secretStore() {
        return new VaultSecretStore(vaultCfg.getRoot());
    }

    @Override
    @Bean
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint vaultEndpoint = VaultEndpoint.create(vaultCfg.getHost(), vaultCfg.getPort());
        vaultEndpoint.setScheme(vaultCfg.getScheme());
        return vaultEndpoint;
    }

    @Bean
    public ClientFactoryWrapper clientHttpRequestFactoryWrapper() {
        return new ClientFactoryWrapper(ClientHttpRequestFactoryFactory.create(
            clientOptions(), sslConfiguration()));
    }


    @Override
    public ClientAuthentication clientAuthentication() {
        if (StringUtils.isNotBlank(vaultCfg.getKeyStoreDirectory())) {
            return new ClientCertificateAuthentication(restOperations());
        } else {
            return new TokenAuthentication(vaultCfg.getToken());
        }
    }

    @Override
    public SslConfiguration sslConfiguration() {
        SslConfiguration.KeyStoreConfiguration keyStoreConfiguration = SslConfiguration.KeyStoreConfiguration.EMPTY;
        SslConfiguration.KeyStoreConfiguration trustStoreConfiguration = SslConfiguration.KeyStoreConfiguration.EMPTY;
        if (StringUtils.isNotBlank(vaultCfg.getKeyStoreDirectory())) {
            keyStoreConfiguration = new SslConfiguration.KeyStoreConfiguration(new FileSystemResource(new File(vaultCfg.getKeyStoreDirectory(), vaultCfg.getKeyStoreName())), vaultCfg.getKeyStorePassword(), vaultCfg.getKeyStoreType());
        }
        if (StringUtils.isNotBlank(vaultCfg.getTrustStoreDirectory())) {
            trustStoreConfiguration = new SslConfiguration.KeyStoreConfiguration(new FileSystemResource(new File(vaultCfg.getTrustStoreDirectory(), vaultCfg.getTrustStoreName())), vaultCfg.getTrustStorePassword(), vaultCfg.getTrustStoreType());
        }
        return new SslConfiguration(keyStoreConfiguration, trustStoreConfiguration);
    }

    @Bean
    public DataSourceCredentialProvider vaultDataSourceCredentialProvider() {
        return new VaultDataSourceCredentialProvider();
    }
}
