package com.thinkbiganalytics.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import javax.annotation.Nonnull;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Constructs an {@link ObjectMapper} for serializing and deserializing JSON in REST calls.
 */
@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    /** Custom object mapper */
    @Nonnull
    private final ObjectMapper objectMapper;

    /**
     * Constructs a {@code JacksonObjectMapperProvider} with a custom object mapper.
     */
    public JacksonObjectMapperProvider() {
        objectMapper = new ObjectMapper();
        objectMapper.setAnnotationIntrospector(AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector()));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return objectMapper;
    }
}
