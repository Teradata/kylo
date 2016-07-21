package com.thinkbiganalytics.metadata.sla.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sr186054 on 7/18/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceLevelAgreementMetric {

    String name();

    String description();

    String shortDescription() default "";
}