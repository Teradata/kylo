/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Replace regex pattern in text with replacement
 */
@Standardizer(name = "Regex Replacement", description = "Replace text based upon a regex pattern")
public class SimpleRegexReplacer implements StandardizationPolicy {
    private static final Logger log = LoggerFactory.getLogger(SimpleRegexReplacer.class);

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
            log.error("Invalid regex [" + e + "].No substitution will be performed.", e);
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
