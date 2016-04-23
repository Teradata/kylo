/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.standardization;

import com.thinkbiganalytics.policies.PolicyProperty;
import com.thinkbiganalytics.policies.PolicyPropertyRef;
import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.standardization.Standardizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Applies a default value in place of null or empty
 */
@Standardizer(name = "Default Value", description = "Applies a default value if null")
public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {

  @PolicyProperty(name = "Default Value", hint = "If the value is null it will use this supplied value")
  private String defaultStr;


  public DefaultValueStandardizer(@PolicyPropertyRef(name = "Default Value") String defaultStr) {
    this.defaultStr = defaultStr;
  }

  @Override
  public String convertValue(String value) {
    return StringUtils.defaultString(value, defaultStr);
  }

  public String getDefaultStr() {
    return defaultStr;
  }

  public void setDefaultStr(String defaultStr) {
    this.defaultStr = defaultStr;
  }
}
