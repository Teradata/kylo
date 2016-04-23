/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.validation.Validator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates value exists in a set
 */
@Validator(name = "Lookup", description = "Must be contained in the list")
public class LookupValidator implements ValidationPolicy<String> {

  @PolicyProperty(name = "List", hint = "Comma separated list of values")
  private String lookupList;

  private Set<String> lookupValues = new HashSet<>();

  public LookupValidator(String... values) {
    lookupValues = new HashSet<>(Arrays.asList(values));
  }

  public LookupValidator(@PolicyPropertyRef(name = "List") String values) {
    this.lookupList = values;
    lookupValues = new HashSet<>(Arrays.asList(StringUtils.split(values, ",")));
  }


  @Override
  public boolean validate(String value) {
    return lookupValues.contains(value);
  }

  public String getLookupList() {
    return lookupList;
  }

  public Set<String> getLookupValues() {
    return lookupValues;
  }
}

