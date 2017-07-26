package com.thinkbiganalytics.json;

/*-
 * #%L
 * thinkbig-commons-util
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Jackson2 JSON parser
 */
public class ObjectMapperSerializer {

    private static final Logger log = LoggerFactory.getLogger(ObjectMapperSerializer.class);


    private static ObjectMapper mapper;

    public ObjectMapperSerializer() {

    }

    /**
     * Return a reference to the Jackson2 ObjectMapper
     *
     * @return the ObjectMapper used to serialize/deserialize
     */
    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }


    /**
     * serialize an Object to a String using Jackson2
     *
     * @param obj the object to serialize
     * @return the JSON string representing the object
     */
    public static String serialize(Object obj) {
        String json = null;
        try {
            json = getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object ",e);
            throw new RuntimeException("Error serializing object", e);
        }
        return json;
    }

    /**
     * deserialize an string as JSON converting it to an object of the supplied class type
     *
     * @param json  the JSON string representing the object
     * @param clazz the class describing the type of object to return
     * @param <T>   the type of object that should be returned
     * @return the deserialized object converted from JSON
     */
    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return getMapper().readValue(json, clazz);
        } catch (IOException e) {
            log.error("Error de-serializing object for class {} ",clazz,e);
            throw new RuntimeException("error de-serializing object", e);
        }
    }

    /**
     * Deserialize JSON with a specific type.
     * Example
     *  List<MyPojo> list = ObjectMapperSerializer.deserialize(json,//new TypeReference<List<MyPojo>>(){});
     *
     * @param json  the JSON string representing the object
     * @param typeReference the type of class to return
     * @param <T> the object type to return
     * @return the deserialized object converted from JSON
     */
    public static <T> T deserialize(String json, TypeReference<T> typeReference){
        try {
            return getMapper().readValue(json, typeReference);
        } catch (IOException e) {
            log.error("Error de-serializing object with TypeReference {} ",typeReference,e);
            throw new RuntimeException("error de-serializing object", e);
        }

    }
}
