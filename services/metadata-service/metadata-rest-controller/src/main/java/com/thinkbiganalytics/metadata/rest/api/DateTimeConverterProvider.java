/**
 *
 */
package com.thinkbiganalytics.metadata.rest.api;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;


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
        return (!DateTime.class.isAssignableFrom(rawType)) ? null : new ParamConverter<T>() {
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
