package com.thinkbiganalytics.ui.rest;

/*-
 * #%L
 * thinkbig-service-controller
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    @Inject
    ApplicationContext applicationContext;


    public JerseyConfig() {
        packages("com.thinkbiganalytics.ui.rest.controller");
        register(JacksonFeature.class);
        register(createJsonProvider());
    }

    @PostConstruct
    private void init() {
        //register any additional beans that are path annotated
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(Path.class);
        if (map != null) {
            Set<Class<?>> pathClasses = map.values().stream().map(o -> o.getClass()).collect(Collectors.toSet());
            registerClasses(pathClasses);
        }
    }

    private JacksonJaxbJsonProvider createJsonProvider() {
        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();

        final ObjectMapper om = new ObjectMapper();
        om.registerModule(new JodaModule());
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        provider.setMapper(om);

        return provider;
    }
}
