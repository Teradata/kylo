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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.support.SslConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.inject.Inject;

@Configuration
@PropertySource(value = "classpath:vault.properties", ignoreResourceNotFound = true)
@ComponentScan
@EnableConfigurationProperties
public class VaultDataSourceCredentialConfig extends AbstractVaultConfiguration {

    @Inject
    private VaultConfiguration vaultConfiguration;

    @Bean(name = "vaultConfiguration")
    @ConfigurationProperties(prefix = "secret.vault")
    public VaultConfiguration vaultConfiguration() {
        return new VaultConfiguration();
    }

    @Bean
    public SecretStore secretStore() {
        return new VaultSecretStore();
    }

    @Override
    @Bean
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint vaultEndpoint = VaultEndpoint.create(vaultConfiguration.getHost(), vaultConfiguration.getPort());
        vaultEndpoint.setScheme(vaultConfiguration.getScheme());
        return vaultEndpoint;
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        //todo replace with client certificate authentication when available
        return new TokenAuthentication(vaultConfiguration.getToken());
    }


    @Override
    public SslConfiguration sslConfiguration() {
        //todo replace with trust store when available
        return SslConfiguration.NONE;
//        return SslConfiguration.forTrustStore(
//            new FileSystemResource(new File(vaultConfiguration.getTrustStoreDirectory(), vaultConfiguration.getTrustStoreName())), vaultConfiguration.getTrustStorePassword());
    }

    @Bean
    public DataSourceCredentialProvider vaultDataSourceCredentialProvider() {
        return new VaultDataSourceCredentialProvider();
    }
}
