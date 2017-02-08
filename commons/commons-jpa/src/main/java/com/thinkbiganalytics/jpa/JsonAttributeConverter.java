/**
 *
 */
package com.thinkbiganalytics.jpa;

/*-
 * #%L
 * thinkbig-commons-jpa
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.persistence.AttributeConverter;

/**
 * A parameterized converter that converts any object that is serializable to a JSON string stored as a.
 * single field.  The resulting serialized string embeds the java class name of the source object plus its
 * JSON serialized form as produced by Jackson.  Implementations must extend this type in order
 * to specify the source object type that is converted.  This subtype is what is specified
 * to the @Converter annotation on the field .
 */
public class JsonAttributeConverter<O> implements AttributeConverter<O, String> {

    static final Logger LOG = LoggerFactory.getLogger(JsonAttributeConverter.class);

    private final ObjectWriter writer;
    private final ObjectReader reader;

//    private Class<? extends Object> type;

    public JsonAttributeConverter() {
//        ResolvableType resType = ResolvableType.forClass(AttributeConverter.class, getClass());
//        Class<? extends Object> objType = (Class<? extends Object>) resType.resolveGeneric(0);
//        this.type = objType;
//        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                                 .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                 .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                 .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

        reader = mapper.reader().forType(JsonWrapper.class);
        writer = mapper.writer().forType(JsonWrapper.class);
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(O attribute) {
        try {
            if (attribute != null) {
                JsonWrapper<O> wrapper = new JsonWrapper<O>(attribute, writer);
                String value = writer.writeValueAsString(wrapper);
                return value;
            } else {
                return null;
            }
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
            if (dbData != null) {
                JsonWrapper<O> wrapper = reader.readValue(dbData);
                O obj = wrapper.readValue(reader);
                return obj;
            } else {
                return null;
            }
        } catch (IOException e) {
            // TODO Throw a runtime exception?
            LOG.error("Failed to deserialize as object from JSON: {}", dbData, e);
            return null;
        }
    }


    public static class JsonWrapper<V> {

        private String type;
        private String value;

        public JsonWrapper() {
        }

        public JsonWrapper(V obj, ObjectWriter writer) {
            Class<V> type = (Class<V>) obj.getClass();

            this.type = obj.getClass().getName();
            try {
                this.value = writer.forType(type).writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                // TODO Throw a runtime exception?
                JsonAttributeConverter.LOG.error("Failed to serialize as object into JSON: {}", obj, e);
            }
        }

        public V readValue(ObjectReader reader) {
            try {
                Class<?> cls = Class.forName(this.type);
                V obj = reader.forType(cls).readValue(this.value);
                return obj;
            } catch (ClassNotFoundException | IOException e) {
                // TODO Throw a runtime exception?
                JsonAttributeConverter.LOG.error("Failed to deserialize as object from JSON: {}", this.value, e);
                return null;
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
