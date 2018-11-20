/**
 * 
 */
package com.thinkbiganalytics.security.core.encrypt;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import javax.inject.Inject;

/**
 * A Jackson deserializer that will decrypt any string value if it is encrypted.
 */
public class EncryptedStringDeserializer extends JsonDeserializer<String> {
    
    @Inject
    private EncryptionService encryptor;

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = p.getText();
        return this.encryptor.isEncrypted(value) ? this.encryptor.decrypt(value) : value;
    }

}
