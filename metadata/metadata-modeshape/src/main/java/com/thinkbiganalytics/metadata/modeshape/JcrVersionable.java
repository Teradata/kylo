package com.thinkbiganalytics.metadata.modeshape;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sr186054 on 6/14/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JcrVersionable {


    /**
     * Supply the Primary Node Type for the Versionable class (i.e.  tba:feed )
     */
    String nodeType();

}
