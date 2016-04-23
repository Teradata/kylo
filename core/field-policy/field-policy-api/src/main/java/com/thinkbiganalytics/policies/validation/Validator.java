/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policies.validation;


import com.thinkbiganalytics.policies.FieldPolicyItem;

import java.io.Serializable;

/**
 * Created by matthutton on 1/20/16.
 */
public interface Validator<T> extends Serializable, FieldPolicyItem {

  boolean validate(T value);
}
