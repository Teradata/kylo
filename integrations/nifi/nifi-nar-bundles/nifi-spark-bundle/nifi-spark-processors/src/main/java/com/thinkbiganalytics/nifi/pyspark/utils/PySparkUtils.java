package com.thinkbiganalytics.nifi.pyspark.utils;

/*-
 * #%L
 * thinkbig-nifi-spark-processors
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

import javax.annotation.Nonnull;

/**
 * Helper utilities for ExecutePySpark processor
 */
public class PySparkUtils {

    private final static String CSV_DELIMITER = ",";
    private final static String PAIR_DELIMITER = "=";

    /**
     * Basic validation to check that a string is for the form value1,value2,value3 and so on
     *
     * @param appArgs String containing value to check
     * @return true/false result of validation
     */
    public boolean validateCsvArgs(@Nonnull String appArgs) {
        if (appArgs.isEmpty()) {
            return false;
        }

        if ((appArgs.charAt(0) == CSV_DELIMITER.charAt(0))
            || (appArgs.charAt(appArgs.length() - 1) == CSV_DELIMITER.charAt(0))) {
            return false;
        }

        String[] providedArgs;

        try {
            providedArgs = appArgs.split(",");
        } catch (Exception e) {
            return false;
        }

        return (providedArgs.length > 0);
    }

    /**
     * Get an array of values that are separated by commas in a single string
     *
     * @param appArgs String containing comma-separated values
     * @return String array of the values
     */
    public String[] getCsvValuesAsArray(@Nonnull String appArgs) {
        if (!validateCsvArgs(appArgs)) {
            return new String[]{};
        }

        return appArgs.split(CSV_DELIMITER);
    }

    /**
     * Get a string of values separated by comma from an array of values
     *
     * @param values Array of string values
     * @return String containing values separated by comma
     */
    public String getCsvStringFromArray(@Nonnull String[] values) {
        if (values.length == 0) {
            return "";
        }

        StringBuilder retVal = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            retVal.append(values[i]);
            if (i != values.length - 1) {
                retVal.append(",");
            }
        }
        return retVal.toString();
    }

    /**
     * Basic validation to check that a string is in the form key1=value1,key2=value2,key3=value3 and so on
     *
     * @param args String containing value to check
     * @return true/false result of validation
     */
    public boolean validateKeyValueArgs(String args) {
        String[] keyValueArgsAsList = getCsvValuesAsArray(args);
        if (keyValueArgsAsList.length == 0) {
            return false;
        }

        for (String keyValuePair : keyValueArgsAsList) {
            if (!validateKeyValuePair(keyValuePair)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Basic validation to check that a string is in the form key=value
     *
     * @param keyValuePair String containing value to check
     * @return true/false result of validation
     */
    public boolean validateKeyValuePair(String keyValuePair) {
        if ((keyValuePair == null) || (keyValuePair.isEmpty())) {
            return false;
        }

        if (keyValuePair.charAt(0) == PAIR_DELIMITER.charAt(0) || (keyValuePair.charAt(keyValuePair.length() - 1) == PAIR_DELIMITER.charAt(0))) {
            return false;
        }

        int equalCount = 0;

        for (int i = 0; i < keyValuePair.length(); i++) {
            if (keyValuePair.charAt(i) == PAIR_DELIMITER.charAt(0)) {
                equalCount++;
            }

            if (equalCount > 1) {
                return false;
            }
        }

        return (equalCount == 1);
    }
}
