/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.policy.standardization;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policy.standardization.Standardizer;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Replace regex pattern in text with replacement
 */
@Standardizer(name = "Regex Replacement", description = "Replace text based upon a regex pattern")
public class SimpleRegexReplacer implements StandardizationPolicy {

  @PolicyProperty(name = "Regex Pattern")
  private String inputPattern;

  private Pattern pattern;
  @PolicyProperty(name = "Replacement", hint = "Text to replace the regex match")
  private String replacement;
  boolean valid;

  public SimpleRegexReplacer(@PolicyPropertyRef(name = "Regex Pattern") String regex,
                             @PolicyPropertyRef(name = "Replacement") String replace) {
    try {
      this.inputPattern = regex;
      this.pattern = Pattern.compile(regex);
      this.replacement = replace;
      valid = true;
    } catch (PatternSyntaxException e) {
      System.out.println("Invalid regex [" + e + "].No substitution will be performed.");
    }
  }

  @Override
  public String convertValue(String value) {
    if (!valid) {
      return value;
    }
    return pattern.matcher(value).replaceAll(replacement);
  }

  public Pattern getPattern() {
    return pattern;
  }

  public String getReplacement() {
    return replacement;
  }

  public boolean isValid() {
    return valid;
  }
}
