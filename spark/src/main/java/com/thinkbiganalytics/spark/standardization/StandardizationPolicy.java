/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization;

import java.io.Serializable;

/**
 * Provides cleansing or data standardization returning a new value from the provided value
 */
public interface StandardizationPolicy extends Serializable {

    String convertValue(String value);
}
