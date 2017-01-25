package com.thinkbiganalytics.activemq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by sr186054 on 3/3/16.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "jms_javatype")
public class TestObject implements Serializable {

    @JsonProperty("name")
    private String name;
    @JsonProperty("age")
    private int age;

    public TestObject(){

    }

    public TestObject(  @JsonProperty("name")String name,   @JsonProperty("age")int age) {
        this.name = name;
        this.age = age;
    }
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("age")
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
