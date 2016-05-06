/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.feedmgr.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by sr186054 on 3/3/16.
 */
public class ObjectMapperSerializer {


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
            e.printStackTrace();
        }
        return json;
    }

    public static <T> T deserialize(String json, Class<T> clazz){
        try {
            return getMapper().readValue(json,clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
