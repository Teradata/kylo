/**
 * 
 */
package com.thinkbiganalytics.security.core;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.thinkbiganalytics.security.core.encrypt.EncryptedStringDeserializer;

/*-
 * #%L
 * kylo-commons-security
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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


import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties.KeyStore;
import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.cloud.context.encrypt.EncryptorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;

import javax.inject.Inject;

/**
 *
 */
@Configuration
@EnableConfigurationProperties
public class SecurityCoreConfig {
    
    @Bean
    @ConfigurationProperties("encrypt")
    public KeyProperties keyProperties() {
        return new KeyProperties();
    }
    
    @Bean
    @ConfigurationProperties("encrypt.rsa")
    public RsaProperties rsaProperties() {
        return new RsaProperties();
    }
    
    @Configuration
    public static class TextEncryptorConfig {
        @Inject
        private KeyProperties keyProperties;
        
        @Inject
        private RsaProperties rsaProperties;
        
        @Bean
        public TextEncryptor textEncryptor() {
            // The presence of an encrypt key takes precedence over the keystore.
            if (StringUtils.isNotBlank(this.keyProperties.getKey())) {
                return new EncryptorFactory().create(this.keyProperties.getKey());
            } else {
                KeyStore store = this.keyProperties.getKeyStore();
                
                if (store.getLocation() == null) {
                    throw new IllegalStateException("Neither an encrypt key or a keystore location specified in the configuration");
                } else if (! store.getLocation().exists()) {
                    throw new IllegalStateException("The specified keystore does not exist: " + store.getLocation());
                } else {
                    return new RsaSecretEncryptor(new KeyStoreKeyFactory(store.getLocation(),
                                                                         store.getPassword().toCharArray()).getKeyPair(store.getAlias(),
                                                                                                                       store.getSecret().toCharArray()),
                                                  this.rsaProperties.getAlgorithm(), this.rsaProperties.getSalt(),
                                                  this.rsaProperties.isStrong());
                }
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptionService encryptionService() {
        return new EncryptionService();
    }
    
    @Bean
    public EncryptedStringDeserializer encryptedStringDeserializer() {
        return new EncryptedStringDeserializer();
    }
    
    @Bean
    public Module decryptionModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, encryptedStringDeserializer());
        return module;
    }
}
