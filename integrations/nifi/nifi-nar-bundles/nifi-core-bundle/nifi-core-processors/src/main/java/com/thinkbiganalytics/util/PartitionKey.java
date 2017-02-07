package com.thinkbiganalytics.util;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.thinkbiganalytics.hive.util.HiveUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
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

    public String getKey() {
        return key;
    }

    public String getKeyForSql() {
        return HiveUtils.quoteIdentifier(key);
    }

    public String getKeyWithAlias() {
        return toAliasSQL() + getKeyForSql();
    }

    public String getFormula() {
        return formula.indexOf('(') > -1 ? formula : HiveUtils.quoteIdentifier(key);
    }

    public String getFormulaWithAlias() {
        surroundFormulaColumnWithTick();
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
     * if column in formula has a space, surround it with a tick mark
     */
    private void surroundFormulaColumnWithTick() {
        int idx = formula.indexOf("(");
        String column = StringUtils.substringBetween(formula, "(", ")");
        if (StringUtils.isNotBlank(column) && column.charAt(0) != '`') {
            column = HiveUtils.quoteIdentifier(column);
            StringBuffer sb = new StringBuffer();
            sb.append(StringUtils.substringBefore(formula, "(")).append("(").append(column).append(")");
            formula = sb.toString();
        }

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
            return toAliasSQL() + getKeyForSql() + "='" + value + "'";
        } else {
            return toAliasSQL() + getKeyForSql() + "=" + value;
        }
    }

    public String getAlias() {
        return alias;
    }

    private void setAlias(String alias) {
        this.alias = alias;
    }

}


