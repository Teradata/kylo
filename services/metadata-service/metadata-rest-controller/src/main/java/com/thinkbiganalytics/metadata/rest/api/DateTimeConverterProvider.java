/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Sean Felten
 */
@Provider
public class DateTimeConverterProvider implements ParamConverterProvider {

    private static final DateTimeFormatter DT_FORMATTER = ISODateTimeFormat.dateTime();
    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.ext.ParamConverterProvider#getConverter(java.lang.Class,
     * java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, Type genericType, Annotation[] annotations) {
        return (! DateTime.class.isAssignableFrom(rawType)) ? null : new ParamConverter<T>() {
            @Override
            public T fromString(String value) {
                return rawType.cast(DT_FORMATTER.parseDateTime(value));
            }

            @Override
            public String toString(T value) {
                return DT_FORMATTER.print((DateTime) value);
            }
        };
    }
}
