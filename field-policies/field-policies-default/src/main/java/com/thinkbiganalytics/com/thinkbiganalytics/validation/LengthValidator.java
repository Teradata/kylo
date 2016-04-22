/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

import org.apache.commons.lang3.Validate;

@FieldValidator(name = "Length", description = "Validate String falls between desired length")
public class LengthValidator implements Validator<String> {

  private int maxLength;
  private int minLength;

  public LengthValidator(int min, int max) {
    Validate.isTrue(min >= 0, "Minimum must be > 0");
    this.minLength = min;
    this.maxLength = max;
  }

  @Override
  public boolean validate(String value) {
    int len = value.length();
    return (len <= maxLength && len >= minLength);
  }

  public int getMaxLength() {
    return maxLength;
  }

  public int getMinLength() {
    return minLength;
  }
}
