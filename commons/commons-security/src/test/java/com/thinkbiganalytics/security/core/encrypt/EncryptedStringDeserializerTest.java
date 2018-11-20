/**
 * 
 */
package com.thinkbiganalytics.security.core.encrypt;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.security.core.SecurityCoreConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityCoreConfig.class, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@TestPropertySource(properties = {
    "encrypt.key=sEIpFztjghM3Ns1HabcQltcBtC7Uiqanb4vI9YWzVnKgzF3VTrrTFQvtGNwQKD4B/XrFdaHJeLid"})
public class EncryptedStringDeserializerTest {
    
    @Inject
    private EncryptionService encryptionService;
    
    @Inject
    @Named("decryptionModule")
    private Module decryptionModule;

    private String json;
    
    @Before
    public void setup() throws JsonProcessingException {
        String encrypted = encryptionService.encrypt("encrypted value");
        Values values = new Values(encrypted, "unencrypted value");
        this.json = new ObjectMapper().writerFor(Values.class).writeValueAsString(values);
    }
    
    @Test
    public void testDecryption() throws IOException {
        Values values = new ObjectMapper().registerModule(this.decryptionModule).readerFor(Values.class).readValue(this.json);
        
        assertThat(values).extracting("encrypted", "unencrypted").containsExactly("encrypted value", "unencrypted value");
    }
    
    public static class Values {
        private String encrypted;
        private String unencrypted;
        
        public Values() {
        }

        public Values(String encrypted, String unencrypted) {
            super();
            this.encrypted = encrypted;
            this.unencrypted = unencrypted;
        }

        public String getEncrypted() {
            return encrypted;
        }

        public void setEncrypted(String encrypted) {
            this.encrypted = encrypted;
        }

        public String getUnencrypted() {
            return unencrypted;
        }

        public void setUnencrypted(String unencrypted) {
            this.unencrypted = unencrypted;
        }
        
    }
}
