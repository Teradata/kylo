/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Created by matthutton on 1/18/16.
 */
public class PartitionKey {
    private String key;
    private String type;
    private String formula;

    public PartitionKey(String key, String type, String formula) {
        Validate.notEmpty(key);
        Validate.notEmpty(type);
        Validate.notEmpty(formula);
        this.key = key.trim().toLowerCase();
        this.type = type.trim().toLowerCase();
        this.formula = formula.trim().toLowerCase();
    }

    public String getKey() {
        return key;
    }

    public String getFormula() {
        return formula;
    }

    /**
     * Generates the where statement against the source table using the provided value
     */
    public String toSourceSQLWhere(String value) {
        if ("string".equalsIgnoreCase(type)) {
            return formula + "='" + value + "'";
        } else {
            return formula + "=" + value;
        }
    }

    /**
     * Generates the where statement against the target table using the provided value
     */
    public String toTargetSQLWhere(String value) {
        return toPartitionNameValue(value);
    }

    /**
     * Generates the partition specification portion using the value
     */
    public String toPartitionNameValue(String value) {
        if ("string".equalsIgnoreCase(type)) {
            return key + "='" + value + "'";
        } else {
            return key + "=" + value;
        }
    }

    public static PartitionKey createFromString(String value) {
        if (StringUtils.isEmpty(value)) return null;
        String[] values = value.split("\\|");
        if (values.length != 3) throw new RuntimeException("Expecting format field|type|formula got " + value);
        return new PartitionKey(values[0], values[1], values[2]);
    }


}


