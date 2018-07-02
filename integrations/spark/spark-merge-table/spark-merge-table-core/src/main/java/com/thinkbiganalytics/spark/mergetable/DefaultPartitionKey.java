/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

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

import java.io.Serializable;
import java.util.Objects;

/**
 */
public class DefaultPartitionKey implements PartitionKey, Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String alias;
    private final String key;
    private final String type;
    private final String formula;

    public DefaultPartitionKey(String key, String type, String formula) {
        this(key, type, formula, null);
    }
    
    public DefaultPartitionKey(String key, String type, String formula, String alias) {
        Validate.notEmpty(key);
        Validate.notEmpty(type);
        Validate.notEmpty(formula);
        this.key = key.trim().toLowerCase();
        this.type = type.trim().toLowerCase();
        this.formula = formula.trim().toLowerCase();
        this.alias = alias;
    }

    public static DefaultPartitionKey createFromString(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String[] values = value.split("\\|");
        if (values.length != 3) {
            throw new RuntimeException("Expecting format field|type|formula got " + value);
        }
        return new DefaultPartitionKey(values[0], values[1], values[2]);
    }

    public static DefaultPartitionKey[] partitionKeysForTableAlias(DefaultPartitionKey[] keys, String alias) {
        DefaultPartitionKey[] partitionKeys = new DefaultPartitionKey[keys.length];
        
        for (int idx = 0; idx < keys.length; idx++) {
            DefaultPartitionKey key = keys[idx];
            partitionKeys[idx] = new DefaultPartitionKey(key.key, key.type, key.formula, alias);
        }
        
        return partitionKeys;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getFormula() {
        return formula.indexOf('(') > -1 ? formula : HiveUtils.quoteIdentifier(key);
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public String getKeyForSql() {
        return HiveUtils.quoteIdentifier(key);
    }

    public String getKeyWithAlias() {
        return toAliasSQL() + getKeyForSql();
    }

    public String getFormulaWithAlias() {
        if (alias != null) {
            String tickedFormula = surroundFormulaColumnWithTick();
            int idx = tickedFormula.indexOf("(");
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
    private String surroundFormulaColumnWithTick() {
        String tickedFormula = this.formula;
        String column = StringUtils.substringBetween(formula, "(", ")");
        if (StringUtils.isNotBlank(column) && column.charAt(0) != '`') {
            column = HiveUtils.quoteIdentifier(column);
            StringBuffer sb = new StringBuffer();
            sb.append(StringUtils.substringBefore(formula, "(")).append("(").append(column).append(")");
            tickedFormula = sb.toString();
        }
        return tickedFormula;
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

    @Override
    public int hashCode() {
        return Objects.hash(PartitionKey.class, this.key, this.type, this.formula, this.alias);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof PartitionKey)) {
            return false;
        } else {
            PartitionKey that = (PartitionKey) obj;
            return Objects.equals(this.key, that.getKey()) &&
                    Objects.equals(this.type, that.getType()) &&
                    Objects.equals(this.formula, that.getFormula()) &&
                    Objects.equals(this.alias, that.getAlias());
        }
                        
    }
}
