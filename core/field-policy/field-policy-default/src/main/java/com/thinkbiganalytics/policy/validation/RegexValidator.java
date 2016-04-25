/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.validation.Validator;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Validator(name = "Regex", description = "Validate Regex Pattern")
public class RegexValidator implements ValidationPolicy<String> {

  @PolicyProperty(name = "Regex expression")
  private String regexExpression;

  private Pattern pattern;
  private boolean valid;

  public RegexValidator(@PolicyPropertyRef(name = "Regex expression") String regex) {
    try {
      this.regexExpression = regex;
      this.pattern = Pattern.compile(regex);
      valid = true;
    } catch (PatternSyntaxException e) {
      System.out.println("Invalid regex [" + e + "]. All values will be valid.");
    }
  }

  @Override
  public boolean validate(String value) {
    if (!valid) {
      return true;
    }
    Matcher matcher = pattern.matcher(value);
    return (matcher.matches());
  }

  public String getRegexExpression() {
    return regexExpression;
  }

  public Pattern getPattern() {
    return pattern;
  }
}
