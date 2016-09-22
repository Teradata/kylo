package com.thinkbiganalytics.policy;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Created by sr186054 on 9/21/16.
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
    
