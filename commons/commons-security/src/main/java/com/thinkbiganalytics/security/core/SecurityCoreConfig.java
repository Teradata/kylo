/**
 * 
 */
package com.thinkbiganalytics.security.core;

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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.config.server.config.EncryptionAutoConfiguration;
import org.springframework.cloud.config.server.encryption.SingleTextEncryptorLocator;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 *
 */
@Configuration
@Import({EncryptionAutoConfiguration.class})
public class SecurityCoreConfig {

    @Bean
    @ConditionalOnMissingBean
    public EncryptionService encryptionService() {
        return new EncryptionService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TextEncryptorLocator textEncryptorLocator(TextEncryptor textEncryptor) {
        return new SingleTextEncryptorLocator(textEncryptor);
    }
}
