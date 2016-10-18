/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

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

    public Set<String> getKeyNames() {
        HashSet<String> keySet = new HashSet<>();
        for (PartitionKey partitionKey : keys) {
            keySet.add(partitionKey.getKey());
        }
        return keySet;
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
     * Generates a select statement that will find all unique data partitions in the source table
     */
    public String toDistinctSelectSQL(String sourceTable, String feedPartitionValue) {
        String[] parts = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            parts[i] = keys.get(i).getFormulaWithAlias();
        }
        return "select " + StringUtils.join(parts, ",") + " , count(0) as tb_cnt from " + sourceTable + " where processing_dttm = '" + feedPartitionValue + "' group by " + StringUtils.join(parts, ",");
    }

    public PartitionSpec newForAlias(String alias) {
        PartitionSpec spec = new PartitionSpec(PartitionKey.partitionKeysForTableAlias(this.keys.toArray(new PartitionKey[0]), alias));
        return spec;
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

}