/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.activemq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by sr186054 on 3/3/16.
 */
public class ObjectMapperSerializer {


    private  ObjectMapper mapper;

    public ObjectMapperSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }


    public  String serialize(Object obj) {
        String json = null;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public  <T> T deserialize(String json, Class<T> clazz){
        try {
            return mapper.readValue(json,clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
