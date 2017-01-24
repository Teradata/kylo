package com.thinkbiganalytics.discovery.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchemaParser {

    String name();

    String description();

    boolean supportsBinary() default false;

    boolean supportsPreview() default true;

    boolean allowSkipHeader() default false;

    boolean generatesHiveSerde() default true;

    String[] tags() default "";

    /**
     * Returns a client-side helper for configuring the parser
     */
    String clientHelper() default "";
}
