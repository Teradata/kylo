/**
 * 
 */
package com.thinkbiganalytics.security.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.config.server.config.EncryptionAutoConfiguration;
import org.springframework.cloud.config.server.encryption.SingleTextEncryptorLocator;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

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
