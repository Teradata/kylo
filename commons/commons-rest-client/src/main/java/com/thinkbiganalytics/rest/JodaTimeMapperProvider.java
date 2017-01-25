package com.thinkbiganalytics.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaMapper;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * A Jersey client provider that adds Joda time mapping  to the Object mapper.
 *
 * @see JodaMapper
 *
 * Created by sr186054 on 10/16/15.
 */
@Provider
public class JodaTimeMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;

    public JodaTimeMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {

         return new JodaMapper();

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
