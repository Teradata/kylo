/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

/**
 * Validates US phone numbers
 */
@FieldValidator(name = "US Zip", description = "Validate US Zip")
public class USZipCodeValidator extends RegexValidator implements Validator<String> {

  private static final USZipCodeValidator instance = new USZipCodeValidator();

  private USZipCodeValidator() {
    super("[0-9]{5}([- /]?[0-9]{4})?$");
  }

  public static USZipCodeValidator instance() {
    return instance;
  }

}