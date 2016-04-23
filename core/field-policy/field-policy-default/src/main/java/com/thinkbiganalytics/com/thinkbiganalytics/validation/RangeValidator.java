/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.PolicyProperty;
import com.thinkbiganalytics.policies.PolicyPropertyRef;
import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

@FieldValidator(name = "Range", description = "Numeric must fall within range")
public class RangeValidator implements Validator<Number> {

  @PolicyProperty(name = "Min", type = PolicyProperty.PROPERTY_TYPE.number, hint = "Minimum Value")
  private Number min;
  @PolicyProperty(name = "Max", type = PolicyProperty.PROPERTY_TYPE.number, hint = "Maximum Value")
  private Number max;

  public RangeValidator(@PolicyPropertyRef(name = "Min") Number min, @PolicyPropertyRef(name = "Max") Number max) {
    super();
    this.min = min;
    this.max = max;
  }

  @Override
  public boolean validate(Number value) {
    return true;
  }

  public Number getMin() {
    return min;
  }

  public Number getMax() {
    return max;
  }
}
