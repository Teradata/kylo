package com.thinkbiganalytics.policy.validation;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Validator(name = "Regex", description = "Validate Regex Pattern")
public class RegexValidator implements ValidationPolicy<String> {

    private static final Logger log = LoggerFactory.getLogger(RegexValidator.class);

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
            log.error("Invalid regex [" + e + "]. All values will be valid.", e);
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
