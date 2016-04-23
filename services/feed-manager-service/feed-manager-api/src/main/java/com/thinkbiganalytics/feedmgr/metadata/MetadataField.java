package com.thinkbiganalytics.feedmgr.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any field Annotated with this will appear in the UI as a expression property ${}
 * the UI project uses beanUtils to inspect and get the value at runtime
 * @see com.thinkbiganalytics.schema.TableSchema.name for an example
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetadataField {
    public boolean enabled() default true;
    public String description() default "";
    public String[] matchingFields() default {};
    public String dataType() default "string";
}
