package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-common
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

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Common utility to discover all classes annotated with a specific class
 */
public class ReflectionPolicyAnnotationDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(ReflectionPolicyAnnotationDiscoverer.class);

    private static Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                                 .setUrls(ClasspathHelper.forJavaClassPath()));

    public ReflectionPolicyAnnotationDiscoverer() {

    }

    public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> clazz) {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(clazz);
        log.info("Found {} classes annotated with {} ", (classes != null ? classes.size() : 0), clazz);
        return classes;

    }
}
    
