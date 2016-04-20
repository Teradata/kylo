/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 *
 * @author Sean Felten
 */
public class JsonAttributeConverter implements AttributeConverter<Object, String> {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonAttributeConverter.class);
    
    private static final ObjectWriter WRITER;
    private static final ObjectReader READER;
    
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        READER = mapper.reader();
        WRITER = mapper.writer();
    }
    

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            return WRITER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            // TODO Throw a runtime exception?
            LOG.error("Failed to serialize as object into JSON: {}", attribute, e);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public Object convertToEntityAttribute(String dbData) {
        try {
            return READER.readValue(dbData);
        } catch (IOException e) {
            // TODO Throw a runtime exception?
            LOG.error("Failed to deserialize as object from JSON: {}", dbData, e);
            return null;
        }
    }

}
