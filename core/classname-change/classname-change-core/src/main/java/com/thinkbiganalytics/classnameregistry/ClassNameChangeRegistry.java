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
                    "Unable to find Class for name {}.  Did this class name change?  If so find the new Class and Annotate it with the @ClassNameChange adding this class {} as a previous class name ",
                    name, name);
                throw e;
            }
        }

    }
}
