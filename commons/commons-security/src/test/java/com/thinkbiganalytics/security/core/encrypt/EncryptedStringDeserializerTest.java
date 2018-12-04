/**
 * 
 */
package com.thinkbiganalytics.security.core.encrypt;

/*-
 * #%L
 * kylo-commons-security
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
