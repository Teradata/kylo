package com.thinkbiganalytics.classnameregistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any classes that are persisted and Change their names over time need to register the old names with the framework so it knows how to load them
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassNameChange {

    String[] classNames();
}