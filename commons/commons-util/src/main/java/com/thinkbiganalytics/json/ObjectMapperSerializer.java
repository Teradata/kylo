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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sr186054 on 3/3/16.
 */
public class ObjectMapperSerializer {
    private static final Logger log = LoggerFactory.getLogger(ObjectMapperSerializer.class);


    private  static ObjectMapper mapper;

    public ObjectMapperSerializer() {

    }
    private static ObjectMapper getMapper(){
        if(mapper == null){
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }


    public static  String serialize(Object obj) {
        String json = null;
        try {
            json = getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing object", e);
        }
        return json;
    }

    public static <T> T deserialize(String json, Class<T> clazz){
        try {
            return getMapper().readValue(json,clazz);
        } catch (IOException e) {
            throw new RuntimeException("error de-serializing object", e);
        }
    }
}
