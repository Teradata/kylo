/**
 * 
 */
package com.thinkbiganalytics.jpa;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

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
public class JsonAttributeConverter<O> implements AttributeConverter<O, String> {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonAttributeConverter.class);
    
    private final ObjectWriter writer;
    private final ObjectReader reader;
    
//    static {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());
//        mapper.setSerializationInclusion(Include.NON_NULL);
//        
//        reader = mapper.reader();
//        writer = mapper.writer();
//        
//    }
    
    private Class<? extends Object> type;
    
    public JsonAttributeConverter() {
        ResolvableType resType = ResolvableType.forClass(AttributeConverter.class, getClass());
        Class<? extends Object> objType = (Class<? extends Object>) resType.resolveGeneric(0);
        this.type = objType;
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        reader = mapper.reader().forType(this.type);
        writer = mapper.writer().forType(this.type);
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(O attribute) {
        try {
            return writer.writeValueAsString(attribute);
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
    public O convertToEntityAttribute(String dbData) {
        try {
            return reader.readValue(dbData);
        } catch (IOException e) {
            // TODO Throw a runtime exception?
            LOG.error("Failed to deserialize as object from JSON: {}", dbData, e);
            return null;
        }
    }

}
