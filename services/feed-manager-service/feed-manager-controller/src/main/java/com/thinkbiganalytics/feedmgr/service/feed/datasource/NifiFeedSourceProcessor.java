package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NifiFeedSourceProcessor {
String nifiProcessorType();
}


