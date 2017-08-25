package com.thinkbiganalytics.policy.standardization;

/*-
 * #%L
 * thinkbig-field-policy-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
    boolean valid;
    @PolicyProperty(name = "Regex Pattern", required = true)
    private String inputPattern;
    private Pattern pattern;
    @PolicyProperty(name = "Replacement", hint = "Text to replace the regex match", placeholder = "")
    private String replacement = "";

    public SimpleRegexReplacer(@PolicyPropertyRef(name = "Regex Pattern") String regex,
                               @PolicyPropertyRef(name = "Replacement") String replace) {
        try {
            this.inputPattern = regex;
            this.pattern = Pattern.compile(regex);
            if (replace != null) {
                this.replacement = replace;
            }
            else {
                this.replacement="";
            }

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
        if(replacement== null){
            replacement = "";
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

    public Boolean accepts (Object value) {
        return (value instanceof String);
    }

    public Object convertRawValue(Object value) {
        if (accepts(value)) {
            return String.valueOf(convertValue(value.toString()));
        }

        return value;
    }
}
