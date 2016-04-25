/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


/**
 * Validates email address
 */
@Validator(name = "IP Address", description = "Valid IP Address")
public class IPAddressValidator extends RegexValidator implements ValidationPolicy<String> {

  private static IPAddressValidator instance = new IPAddressValidator();

  private IPAddressValidator() {
    super(
        "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$");
  }

  public static IPAddressValidator instance() {
    return instance;
  }

}
