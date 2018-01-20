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


import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the date matches a given format of yyyy-MM-dd
 */
@Validator(name = "Date", description = "Validate Hive-friendly date format")
public class DateValidator implements ValidationPolicy<String> {
    private static final Logger log = LoggerFactory.getLogger(DateValidator.class);

    private static final DateValidator instance = new DateValidator();
    private static final DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final int LENGTH = 10;

    private DateValidator() {
        super();
    }

    public static DateValidator instance() {
        return instance;
    }

    @Override
    public boolean validate(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                parseDate(value);
                return true;

            } catch (IllegalArgumentException e) {
                log.debug("Invalid date format [{}]", value);
                return false;
            }
        }
        return false;
    }

    /**
     * Parses the string date and returns the
     */
    public DateTime parseDate(String value) {
        int cnt = value.length();
        if (cnt == LENGTH) {
            return DATE.parseDateTime(value);
        } else {
            throw new IllegalArgumentException("Expecting yyyy-MM-dd");
        }
    }

}
