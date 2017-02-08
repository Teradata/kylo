package com.thinkbiganalytics.classnameregistry;

/*-
 * #%L
 * thinkbig-classname-change-core
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

import com.thinkbiganalytics.policy.ReflectionPolicyAnnotationDiscoverer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * This class will help find Classes that have moved or changed names. If a Class is written as JSON and then needs to be reconstructed it needs to know the Class type/name to create. If that class
 * changes Name/pkg overtime the Object will not be able to be constructed because the Class changed. To fix this when a class changes the is persisted/saved as JSON Annotate that class with the
 *
 * @ClassNameChange and then attempt to find that class using this Registry
 */
public class ClassNameChangeRegistry {

    private static final Logger log = LoggerFactory.getLogger(ClassNameChangeRegistry.class);

    private static Map<String, Class> classRegistry = new HashMap<>();

    public static Class findClass(String name) throws ClassNotFoundException {
        try {
            Class clazz = Class.forName(name);
            return clazz;
        } catch (ClassNotFoundException e) {
            if (classRegistry.containsKey(name)) {
                return classRegistry.get(name);
            } else {
                log.warn(
                    "Unable to find Class: {}.  Are you missing a jar plugin?  Please be sure all plugins are included in the /plugin directory.  Did this class name change?  If so find the new Class and annotate it with the @ClassNameChange(classNames:{\"{}\"})",
                    name, name);
                throw e;
            }
        }

    }

    public void loadRegistry() {
        Set<Class<?>>
            classes = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(ClassNameChange.class);
        if (classes != null && !classes.isEmpty()) {
            for (Class clazz : classes) {
                ClassNameChange classNameChange = (ClassNameChange) clazz.getAnnotation(ClassNameChange.class);
                for (String oldClassName : classNameChange.classNames()) {
                    classRegistry.put(oldClassName, clazz);
                }
            }
        }
    }

    @PostConstruct
    private void init() {
        loadRegistry();
    }
}
