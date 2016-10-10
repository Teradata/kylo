/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by matthutton on 1/18/16.
 */
public class PartitionKey implements Cloneable {

    private String alias;
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

    private void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKey() {
        return key;
    }

    public String getKeyWithAlias() {
        return toAliasSQL() + key;
    }

    public String getFormula() {
        return formula;
    }

    public String getFormulaWithAlias() {
        if (alias != null) {
            int idx = formula.indexOf("(");
            if (idx > -1) {
                StringBuffer sb = new StringBuffer(formula);
                sb.insert(idx + 1, alias + ".");
                return sb.toString();
            }
            return getKeyWithAlias();
        }
        return getFormula();
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

    private String toAliasSQL() {
        return (alias != null ? alias + "." : "");
    }

    /**
     * Generates the partition specification portion using the value
     */
    public String toPartitionNameValue(String value) {
        if ("string".equalsIgnoreCase(type)) {
            return toAliasSQL() + key + "='" + value + "'";
        } else {
            return toAliasSQL() + key + "=" + value;
        }
    }

    public String getAlias() {
        return alias;
    }

    public static PartitionKey createFromString(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String[] values = value.split("\\|");
        if (values.length != 3) {
            throw new RuntimeException("Expecting format field|type|formula got " + value);
        }
        return new PartitionKey(values[0], values[1], values[2]);
    }

    public static PartitionKey[] partitionKeysForTableAlias(PartitionKey[] keys, String alias) {
        List<PartitionKey> partitionKeys = new ArrayList<>();

        Arrays.stream(keys).forEach(key -> {
            try {
                PartitionKey clonedKey = (PartitionKey) key.clone();
                clonedKey.setAlias(alias);
                partitionKeys.add(clonedKey);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        return partitionKeys.toArray(new PartitionKey[0]);
    }

}


