package com.thinkbiganalytics.feedmgr.rest.beanvalidation;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
/**
 * Created by Jeremy Merrifield on 6/14/16.
 *
 * Validate the properties are correct for a new feed category
 */
@Target({ElementType.FIELD,ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewFeedCategory.Validator.class)
public @interface NewFeedCategory {
    String message() default "The minimum required properties were not included for creation of a new category";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    public class Validator implements ConstraintValidator<NewFeedCategory, FeedCategory> {

        @Override
        public void initialize(final NewFeedCategory newFeedCategory) {
        }

        @Override
        public boolean isValid(final FeedCategory feedCategory, final ConstraintValidatorContext constraintValidatorContext) {
            return feedCategory != null && !StringUtils.isEmpty(feedCategory.getName())  ;
        }

    }
}
