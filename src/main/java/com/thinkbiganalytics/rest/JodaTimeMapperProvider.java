package com.thinkbiganalytics.rest;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.joda.JodaMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.thinkbiganalytics.jira.domain.GetIssue;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by sr186054 on 10/16/15.
 */
@Provider
public class JodaTimeMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;
   // final ObjectMapper combinedObjectMapper;

    public JodaTimeMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
     //   combinedObjectMapper = createCombinedObjectMapper();
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {

        if (type == GetIssue.class) {
            return new JodaMapper();
        } else {
            return defaultObjectMapper;
        }
    }

    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);

        return result;
    }

    private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {

        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();

        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }
}