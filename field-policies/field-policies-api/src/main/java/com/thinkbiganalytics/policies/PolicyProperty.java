package com.thinkbiganalytics.policies;

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

  enum PROPERTY_TYPE {
    number, string, select, regex, date
  }

  String name();

  String displayName() default "";

  String value() default "";

  String placeholder() default "";

  PROPERTY_TYPE type() default PROPERTY_TYPE.string;

  String hint() default "";

  PropertyLabelValue[] labelValues() default {};

  String[] selectableValues() default {};
}

