package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-api
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sr186054 on 4/21/16.
 * Annotation that is used to display input form elements
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface PolicyProperty {


  /**
   * internal name for the property
   */
  String name();

  /**
   * The label for the form item
   * @return
   */
  String displayName() default "";

  /**
   * The value of the input.  The form will default to this value if it is supplied
   * @return
   */
  String value() default "";

  /**
   * Input placeholder with text indicating what should be entered
   * @return
   */
  String placeholder() default "";

  /**
   * Determines how the property should be rendered:
   * number, string, select, regex, date, chips, feedChips, currentFeed, currentFeedCronSchedule, feedSelect, email, cron
   * type of {@code PROPERTY_TYPE.string} will be rendered as a standard input box.
   * You can enforce specific text expressions using the {@code pattern} property passing in the valid Regexp
   * @return
   */
  PolicyPropertyTypes.PROPERTY_TYPE type() default PolicyPropertyTypes.PROPERTY_TYPE.string;

  /**
   * Helpful text that will be rendered below the input entry
   * @return
   */
  String hint() default "";

  /**
   * For Select inputs this is a list of the options that will be available.  This cannot be used with the {@code selectableValues()}
   * @return
   */
  PropertyLabelValue[] labelValues() default {};


  /**
   * For a select input this is the list of optoins that will be available.  This cannot be used with the {@code labelValues()}
   * @return
   */
  String[] selectableValues() default {};

  /**
   * Used for rendering {@code PROPERTY_TYPE.chips} where the input has multiple values
   * @return
   */
  PropertyLabelValue[] values() default {};

  /**
   * requires input from the user.
   *
   * @return
   */
  boolean required() default false;

  /**
   * Indicates the input will be hidde
   * @return
   */
  boolean hidden() default false;

  /**
   * A arbitary string used to group inputs together.
   * Items with the same group name will be displayed together in the same row
   * @return
   */
  String group() default "";

  /**
   * Regexp used to evaluate the input.
   * This should not include the beginning and ending slashes
   * Example. To validate a Numeric input one should use: ^\d+$  instead of this: /^\d+$/
   * @return
   */
  String pattern() default "";

  /**
   * The message to be displayed if the pattern is supplied and the user enters something inavalid
   */
  String patternInvalidMessage() default "Invalid Input";
}

