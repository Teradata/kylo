package com.thinkbiganalytics.classnameregistry;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 8/6/16.
 * This class will help find Classes that have moved or changed names.
 * If a Class is written as JSON and then needs to be reconstructed it needs to know the Class type/name to create. If that class changes Name/pkg overtime the Object will not be able to be constructed because the Class changed.  To fix this when a class changes the is persisted/saved as JSON Annotate that class with the @ClassNameChange and then attempt to find that class using this Registry
 *
 */
public class ClassNameChangeRegistry {

    private static final Logger log = LoggerFactory.getLogger(ClassNameChangeRegistry.class);

    private static Map<String, Class> classRegistry = new HashMap<>();

    public void loadRegistry() {
        Set<Class<?>>
            classes = new Reflections().getTypesAnnotatedWith(ClassNameChange.class);
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
}
