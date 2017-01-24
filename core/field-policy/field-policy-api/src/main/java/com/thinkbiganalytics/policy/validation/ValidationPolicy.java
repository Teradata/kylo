package com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.FieldPolicyItem;

import java.io.Serializable;

/**
 * Created by matthutton on 1/20/16.
 */
public interface ValidationPolicy<T> extends Serializable, FieldPolicyItem {

  boolean validate(T value);


}
