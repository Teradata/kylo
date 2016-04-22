/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.PolicyProperty;
import com.thinkbiganalytics.policies.PolicyPropertyRef;
import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@FieldValidator(name = "Regex", description = "Validate Regex Pattern")
public class RegexValidator implements Validator<String> {

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
