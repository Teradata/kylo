package com.thinkbiganalytics.policy.precondition;

/*-
 * #%L
 * thinkbig-precondition-api
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
 * Annotate a {@link Precondition} with this class so it can appear in the Kylo User Interface when scheduling a feed
 *
 * Refer to the precondition-default module for examples.
 * The feed-manager-precondition-policy module finds all classes with this annotation and processes them for UI display
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PreconditionPolicy {

    /**
     * The name of the precondition policy.  This will be displayed as the name in the UI
     */
    String name();

    /**
     * The description of the precondition.  This will be displayed in the UI.
     */
    String description();

    /**
     * A shorter description.
     */
    String shortDescription() default "";
}
