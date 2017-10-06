package com.thinkbiganalytics.feedmgr.rest.beanvalidation;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;

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
 * Validate the properties are correct for a new feed category
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewFeedCategory.Validator.class)
public @interface NewFeedCategory {

    String message() default "The minimum required properties were not included for creation of a new category";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<NewFeedCategory, FeedCategory> {

        @Override
        public void initialize(final NewFeedCategory newFeedCategory) {
        }

        @Override
        public boolean isValid(final FeedCategory feedCategory, final ConstraintValidatorContext constraintValidatorContext) {
            if (feedCategory == null)
                return false;
            if (StringUtils.isEmpty(feedCategory.getName()))
                return false;
            if (StringUtils.isEmpty(feedCategory.getSystemName()))
                return false;

            //we must be receiving a valid system name
            return feedCategory.getSystemName().equals(SystemNamingService.generateSystemName(feedCategory.getSystemName()));
        }

    }
}
