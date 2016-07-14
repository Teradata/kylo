package com.thinkbiganalytics.policy.precondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sr186054 on 4/21/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PreconditionPolicy {

    String name();

    String description();

    String shortDescription() default "";
}
