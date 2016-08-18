/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sr186054 on 3/3/16.
 */
public class ObjectMapperSerializer {
    private static final Logger log = LoggerFactory.getLogger(ObjectMapperSerializer.class);


    private  ObjectMapper mapper;

    public ObjectMapperSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JodaModule());
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public  String serialize(Object obj) {
        String json = null;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing object", e);
        }
        return json;
    }

    public  <T> T deserialize(String json, Class<T> clazz){
        try {
            return mapper.readValue(json,clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error de-serializing object", e);
        }
    }
}
