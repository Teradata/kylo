package com.thinkbiganalytics.policy;

import com.thinkbiganalytics.policy.validation.PolicyPropertyTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sr186054 on 4/21/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface PolicyProperty {



  String name();

  String displayName() default "";

  String value() default "";

  String placeholder() default "";

  PolicyPropertyTypes.PROPERTY_TYPE type() default PolicyPropertyTypes.PROPERTY_TYPE.string;

  String hint() default "";

  PropertyLabelValue[] labelValues() default {};

  String[] selectableValues() default {};

  PropertyLabelValue[] values() default {};

  boolean required() default false;

  boolean hidden() default false;

  String group() default "";
}

