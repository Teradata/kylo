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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents a partition specification for a target table
 */
public class PartitionSpec implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(PartitionSpec.class);

    private List<PartitionKey> keys;

    public PartitionSpec(PartitionKey... partitionKeys) {
        super();
        keys = Arrays.asList(partitionKeys);
    }

    /**
     * Creates partition keys from a string specification in format: field|type|formula\n
     * format, e.g.
     * year|string|year(hired)
     * month|int|month(hired)
     * country|int|country
     **/
    public PartitionSpec(String spec) {
        super();
        keys = new Vector<>();
        if (!StringUtils.isEmpty(spec)) {
            try (BufferedReader br = new BufferedReader(new StringReader(spec))) {

                String line = null;
                while ((line = br.readLine()) != null) {
                    PartitionKey partitionKey = PartitionKey.createFromString(line);
                    if (partitionKey != null) {
                        keys.add(partitionKey);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to process specification [" + spec + "]");
            }
        }
    }

    public static void main(String[] args) {
        PartitionKey key1 = new PartitionKey("country", "string", "country");
        PartitionKey key2 = new PartitionKey("year", "int", "year(hired)");
        PartitionKey key3 = new PartitionKey("month", "int", "month(hired)");

        PartitionSpec spec = new PartitionSpec(key1, key2, key3);
        String[] selectFields = new String[]{"id", "name", "company", "zip", "phone", "email", "hired"};
        String selectSQL = StringUtils.join(selectFields, ",");

        String[] values = new String[]{"USA", "2015", "4"};

        String targetSqlWhereClause = spec.toTargetSQLWhere(values);
        String sourceSqlWhereClause = spec.toSourceSQLWhere(values);
        String partitionClause = spec.toPartitionSpec(values);

        /*
             insert overwrite table employee partition (year=2015,month=10,country='USA')
             select id, name, company, zip, phone, email, hired from employee_feed
             where year(hired)=2015 and month(hired)=10 and country='USA'
             union distinct
             select id, name, company, zip, phone, email, hired from employee
             where year=2015 and month=10 and country='USA'
         */

        String targetTable = "employee";
        String sourceTable = "employee_feed";
        String sqlWhere = "employee_feed";

        StringBuffer sb = new StringBuffer();
        sb.append("insert overwrite table ").append(targetTable).append(" ")
            .append(partitionClause)
            .append(" select ").append(selectSQL)
            .append(" from ").append(sourceTable).append(" ")
            .append(" where ")
            .append(sourceSqlWhereClause)
            .append(" union distinct ")
            .append(" select ").append(selectSQL)
            .append(" from ").append(targetTable).append(" ")
            .append(" where ")
            .append(targetSqlWhereClause);

        log.info(sb.toString());
    }

    public Set<String> getKeyNames() {
        HashSet<String> keySet = new HashSet<>();
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
    public String toTargetSQLWhere(String[] values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toTargetSQLWhere(values[i]);
        }
        return StringUtils.join(parts, " and ");
    }

    public String toSourceSQLWhere(String[] values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toSourceSQLWhere(values[i]);
        }
        return StringUtils.join(parts, " and ");
    }

    public String toPartitionSpec(String[] values) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).toPartitionNameValue(values[i]);
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
        final String keysWithAliases = keys.stream()
            .map(PartitionKey::getFormulaWithAlias)
            .collect(Collectors.joining(", "));
        return "select " + keysWithAliases + ", count(0) as `tb_cnt` from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) +
               " where `processing_dttm` = " + HiveUtils.quoteString(feedPartitionValue) +
               " group by " + keysWithAliases;
    }

    public PartitionSpec newForAlias(String alias) {
        return new PartitionSpec(PartitionKey.partitionKeysForTableAlias(this.keys.toArray(new PartitionKey[0]), alias));
    }

}
