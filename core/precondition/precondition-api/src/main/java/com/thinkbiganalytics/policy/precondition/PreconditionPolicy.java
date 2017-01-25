package com.thinkbiganalytics.policy.precondition;

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
     * @return
     */
    String description();

    /**
     * A shorter description.
     * @return
     */
    String shortDescription() default "";
}
