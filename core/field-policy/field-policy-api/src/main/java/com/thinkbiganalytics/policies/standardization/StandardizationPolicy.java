/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policies.standardization;

import com.thinkbiganalytics.policies.FieldPolicyItem;

import java.io.Serializable;

/**
 * Provides cleansing or data standardization returning a new value from the provided value
 */
public interface StandardizationPolicy extends Serializable, FieldPolicyItem {

  String convertValue(String value);

}
