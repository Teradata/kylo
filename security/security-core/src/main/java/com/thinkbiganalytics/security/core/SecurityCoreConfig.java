/**
 * 
 */
package com.thinkbiganalytics.security.core;

import org.springframework.cloud.config.server.encryption.SingleTextEncryptorLocator;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.thinkbiganalytics.security.core.encrypt.EncryptionService;

/**
 *
 */
@Configuration
public class SecurityCoreConfig {

    @Bean
    public EncryptionService encryptionService() {
        return new EncryptionService();
    }

    @Bean
    public TextEncryptorLocator textEncryptorLocator(TextEncryptor textEncryptor) {
        return new SingleTextEncryptorLocator(textEncryptor);
    }
}
