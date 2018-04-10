/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/*-
 * #%L
 * kylo-spark-merge-table-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;

/**
 *
 */
public class DefaultPartitionSpec implements PartitionSpec, Serializable {
    
    private static final long serialVersionUID = 1L;

    private List<PartitionKey> keys;
    
    public DefaultPartitionSpec(PartitionKey... partitionKeys) {
        super();
        keys = Arrays.asList(partitionKeys);
    }

    public DefaultPartitionSpec(List<PartitionKey> partitionKeys) {
        super();
        keys = new ArrayList<>(partitionKeys);
    }

    /**
     * Creates partition keys from a string specification in format: field|type|formula\n
     * format, e.g.
     * year|string|year(hired)
     * month|int|month(hired)
     * country|int|country
     **/
    public DefaultPartitionSpec(String spec) {
        super();
        keys = new Vector<>();
        if (!StringUtils.isEmpty(spec)) {
            try (BufferedReader br = new BufferedReader(new StringReader(spec))) {

                String line = null;
                while ((line = br.readLine()) != null) {
                    DefaultPartitionKey partitionKey = DefaultPartitionKey.createFromString(line);
                    if (partitionKey != null) {
                        keys.add(partitionKey);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to process specification [" + spec + "]");
            }
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.PartitionSpec#getKeys()
     */
    @Override
    public List<PartitionKey> getKeys() {
        return this.keys;
    }

    public List<String> getKeyNames() {
        List<String> keySet = new ArrayList<>();
        for (PartitionKey partitionKey : keys) {
            keySet.add(partitionKey.getKey());
        }
        return keySet;
    }

    public boolean isNonPartitioned() {
        return keys.size() == 0;
    }

    /**
     * Generates a where clause against the target table using the partition keys
     */
    public String toTargetSQLWhere(List<String> values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toTargetSQLWhere(values.get(i));
        }
        return StringUtils.join(parts, " and ");
    }

    public String toSourceSQLWhere(List<String> values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toSourceSQLWhere(values.get(i));
        }
        return StringUtils.join(parts, " and ");
    }

    public String toPartitionSpec(List<String> values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toPartitionNameValue(values.get(i));
        }
        return "partition (" + StringUtils.join(parts, ",") + ")";
    }

    public String toDynamicPartitionSpec() {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).getKeyWithAlias();
        }
        return "partition (" + toPartitionSelectSQL() + ")";
    }

    public String toPartitionSelectSQL() {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).getKeyWithAlias();
        }
        return StringUtils.join(parts, ",");
    }

    public String toDynamicSelectSQLSpec() {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).getFormulaWithAlias() + " " + keys.get(i).getKeyForSql();
        }
        return StringUtils.join(parts, ",");
    }

    /**
     * Generates a select statement that will find all unique data partitions in the source table.
     *
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param feedPartitionValue the source processing partition value
     */
    public String toDistinctSelectSQL(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String feedPartitionValue) {
        String[] keysWithAliases = new String[keys.size()];
        for (int idx = 0; idx < keys.size(); idx++) {
            keysWithAliases[idx] = keys.get(idx).getFormulaWithAlias();
        }
        
        String columns = StringUtils.join(keysWithAliases, ", ");
        return "select " + columns + ", count(0) as `tb_cnt` from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) +
               " where `processing_dttm` = " + HiveUtils.quoteString(feedPartitionValue) +
               " group by " + columns;
    }

    public PartitionSpec withAlias(String alias) {
        return new DefaultPartitionSpec(DefaultPartitionKey.partitionKeysForTableAlias(this.keys.toArray(new DefaultPartitionKey[0]), alias));
    }

}
