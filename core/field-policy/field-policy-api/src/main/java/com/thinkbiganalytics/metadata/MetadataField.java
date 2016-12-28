/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any field Annotated with this will appear in the UI as a expression property ${}
 * the UI project uses beanUtils to inspect and get the value at runtime
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetadataField {
    boolean enabled() default true;
    String description() default "";
    String[] matchingFields() default {};
    String dataType() default "string";
}
