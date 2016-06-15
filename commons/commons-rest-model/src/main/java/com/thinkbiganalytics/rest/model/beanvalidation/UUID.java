package com.thinkbiganalytics.rest.model.beanvalidation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

/**
 * Created by Jeremy Merrifield on 6/14/16.
 */
@Target({ElementType.FIELD,ElementType.METHOD, ElementType.PARAMETER})
@Constraint(validatedBy={})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
public @interface UUID {
    String message() default "Invalid UUID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
