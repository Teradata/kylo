package com.thinkbiganalytics.ingest;

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
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.PartitionBatch;
import com.thinkbiganalytics.util.PartitionSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Merge or Sync from a table into a target table. Dedupes and uses partition strategy of the target table. Sync will completely replace the target table with the contents from the source.  Merge will
 * append the data into the target table adhering to partitions if defined.  If Dedupe is specified then duplicates will be stripped.
 */
public class TableMergeSyncSupport implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TableMergeSyncSupport.class);

    protected Connection conn;

    public TableMergeSyncSupport(Connection conn) {
        Validate.notNull(conn);
        this.conn = conn;
    }

    /**
     * Sets several hive parameters to enable dynamic partitions
     */
    public void enableDynamicPartitions() {
        doExecuteSQL("set hive.exec.dynamic.partition=true");
        doExecuteSQL("set hive.exec.dynamic.partition.mode=nonstrict");
        // Required for ORC and Parquet
        doExecuteSQL("set hive.optimize.index.filter=false");
    }

    /**
     * Sets the list of configurations given in name=value string pairs
     */
    public void setHiveConf(String[] configurations) {
        for (String conf : configurations) {
            if(conf.equalsIgnoreCase("reset")){
                    resetHiveConf();
            }
            else {
                doExecuteSQL("set " + conf);
            }
        }
    }

    /**
     * Sets the list of configurations given in name=value string pairs
     */
    public void resetHiveConf() {
            doExecuteSQL("reset");
    }


    public String getHivePropertyValue(String parameter) throws SQLException{

        String value = null;
        try (final Statement st = conn.createStatement()) {
            ResultSet rs = doSelectSQL(st, "set "+parameter);

            while (rs.next()) {
                value = rs.getString(1);
                logger.info("Value = {} ",value);
            }
        }
        return value;
    }


    /**
     * Performs a sync replacing all data in the target table. A temporary table is created with the new data, old table dropped and the temporary table renamed to become the new table.  This causes a
     * very brief lapse for consumers between when the table is dropped and the rename.
     *
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     */
    public void doSync(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable,
                       @Nonnull final PartitionSpec partitionSpec, @Nonnull final String feedPartitionValue) throws SQLException {
        // Validate input parameters
        Validate.notEmpty(sourceSchema);
        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetSchema);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);

        // Extract the existing HDFS location of data
        String refTableLocation = extractTableLocation(targetSchema, targetTable);

        // 1. Create a temporary "sync" table for storing our latest snapshot
        String syncTableLocation = deriveSyncTableLocation(targetTable, refTableLocation);
        String syncTable = createSyncTable(targetSchema, targetTable, syncTableLocation);

        // 2. Populate the temporary "sync" table
        final String[] selectFields = getSelectFields(sourceSchema, sourceTable, targetSchema, syncTable, partitionSpec);
        final String syncSQL = partitionSpec.isNonPartitioned()
                               ? generateSyncNonPartitionQuery(selectFields, sourceSchema, sourceTable, targetSchema, syncTable, feedPartitionValue)
                               : generateSyncDynamicPartitionQuery(selectFields, partitionSpec, sourceSchema, sourceTable, targetSchema, syncTable, feedPartitionValue);
        doExecuteSQL(syncSQL);

        // 3. Drop the sync table. Since it is a managed table it will drop the old data
        dropTable(targetSchema, targetTable);

        // 4. Rename the sync table
        renameTable(targetSchema, syncTable, targetTable);
    }

    /**
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     */
    public void doRollingSync(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable,
                              @Nonnull final PartitionSpec partitionSpec, @Nonnull final String feedPartitionValue) throws SQLException {
        // Validate input parameters
        Validate.notEmpty(sourceSchema);
        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetSchema);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);

        List<PartitionBatch> batches = createPartitionBatches(partitionSpec, sourceSchema, sourceTable, feedPartitionValue);

        final String[] selectFields = getSelectFields(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec);

        final String syncSQL = generateRollingSyncQuery(selectFields, partitionSpec, sourceSchema, sourceTable, targetSchema, targetTable, batches, feedPartitionValue);

        doExecuteSQL(syncSQL);

    }

    private String generateRollingSyncQuery(String[] selectFields, PartitionSpec partitionSpec, String sourceSchema,
                                            String sourceTable, String targetSchema, String targetTable,
                                            List<PartitionBatch> batches, String feedPartitionValue) {

        final String selectSQL = StringUtils.join(selectFields, ",");
        String partitionWhere = targetPartitionsWhereClause(batches, true);

        //something went horribly wrong if there are no partitions
        Validate.notEmpty(partitionWhere);

        return "insert overwrite table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               partitionSpec.toDynamicPartitionSpec() +
               " select " + selectSQL + "," + partitionSpec.toDynamicSelectSQLSpec() +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) +
               " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue)
               + " and (" + partitionWhere + ")";
    }

    /**
     * Performs the doMerge and insert into the target table from the source table.
     *
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     * @param shouldDedupe       whether to perform dedupe during merge
     */
    public List<PartitionBatch> doMerge(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable,
                                        @Nonnull final PartitionSpec partitionSpec, @Nonnull final String feedPartitionValue, final boolean shouldDedupe) {
        // Validate input parameters
        Validate.notEmpty(sourceSchema);
        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetSchema);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);

        List<PartitionBatch> batches = null;

        final String[] selectFields = getSelectFields(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec);
        final String sql;
        if (partitionSpec.isNonPartitioned()) {
            if (shouldDedupe) {
                if (hasProcessingDttm(selectFields)) {
                    sql = generateMergeNonPartitionQueryWithDedupe(selectFields, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
                } else {
                    sql = generateMergeNonPartitionQueryWithDedupeNoProcessingDttm(selectFields, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
                }
            } else {
                sql = generateMergeNonPartitionQuery(selectFields, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
            }
        } else {
            if (shouldDedupe) {
                batches = createPartitionBatches(partitionSpec, sourceSchema, sourceTable, feedPartitionValue);
                // Newer tables with processing_dttm in target will always be unique so requires additional handling
                if (hasProcessingDttm(selectFields)) {
                    sql = generateMergeWithDedupePartitionQuery(selectFields, partitionSpec, batches, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
                } else {
                    sql = generateMergeWithDedupePartitionQueryNoProcessingDttm(selectFields, partitionSpec, batches, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
                }

            } else {
                sql = generateMergeWithPartitionQuery(selectFields, partitionSpec, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue);
            }
        }
        doExecuteSQL(sql);
        return batches;
    }

    private boolean hasProcessingDttm(String[] selectFields) {
        return Arrays.asList(selectFields).stream().anyMatch(v -> ("`processing_dttm`".equals(v)));
    }

    /**
     * Updates any rows matching the same primary key, otherwise inserts the value into the appropriate partition.
     *
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     * @param columnSpecs        the columns to join on
     */
    public void doPKMerge(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable,
                          @Nonnull final PartitionSpec partitionSpec, @Nonnull final String feedPartitionValue, @Nonnull final ColumnSpec[] columnSpecs) {
        // Validate input parameters
        Validate.notEmpty(sourceSchema);
        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetSchema);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);
        Validate.notEmpty(columnSpecs);

        final String[] selectFields = getSelectFields(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec);
        final String sql = partitionSpec.isNonPartitioned()
                           ? generatePKMergeNonPartitionQuery(selectFields, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue, columnSpecs)
                           : generatePKMergePartitionQuery(selectFields, partitionSpec, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue, columnSpecs);
        doExecuteSQL(sql);
    }

    /**
     * Create a new table like the old table with the new location.
     *
     * @param schema            the schema or database name for the reference table
     * @param table             the name of the reference table
     * @param syncTableLocation the HDFS location for the reference table
     * @return the new table name
     */
    private String createSyncTable(@Nonnull final String schema, @Nonnull final String table, @Nonnull final String syncTableLocation) throws SQLException {
        final String syncTable = table + "_" + System.currentTimeMillis();
        final String createSQL = "create table " + HiveUtils.quoteIdentifier(schema, syncTable) +
                                 " like " + HiveUtils.quoteIdentifier(schema, table) +
                                 " location " + HiveUtils.quoteString(syncTableLocation);
        doExecuteSQL(createSQL);
        return syncTable;
    }

    /**
     * Drop table removing the data.
     *
     * @param schema the schema or database name containing the table
     * @param table  the name of the table
     */
    public void dropTable(@Nonnull final String schema, @Nonnull final String table) {
        // Make managed to remove the old data
        String makeManagedSQL = "alter table " + HiveUtils.quoteIdentifier(schema, table) + " SET TBLPROPERTIES ('EXTERNAL'='FALSE')";
        doExecuteSQL(makeManagedSQL);
        String sql = "DROP TABLE " + HiveUtils.quoteIdentifier(schema, table);
        doExecuteSQL(sql);
    }

    /**
     * Renames the specified table.
     *
     * @param schema  the schema or database name containing the tables
     * @param oldName the name of the table to be renamed
     * @param newName the new name for the table
     */
    public void renameTable(@Nonnull final String schema, @Nonnull final String oldName, @Nonnull final String newName) {
        final String sql = "alter table " + HiveUtils.quoteIdentifier(schema, oldName) + " RENAME TO " + HiveUtils.quoteIdentifier(schema, newName);
        doExecuteSQL(sql);
    }

    /**
     * Create a new HDFS location for the target data
     *
     * @param table       the name of the table
     * @param oldLocation the old location
     * @return the new HDFS location
     */
    private String deriveSyncTableLocation(String table, String oldLocation) {
        String[] parts = oldLocation.split("/");
        parts[parts.length - 1] = table + "_" + System.currentTimeMillis();
        return StringUtils.join(parts, "/");
    }

    /**
     * Extract the HDFS location of the table data.
     *
     * @param schema the schema or database name
     * @param table  the table name
     * @return the HDFS location of the table data
     */
    private String extractTableLocation(@Nonnull final String schema, @Nonnull final String table) throws SQLException {
        doExecuteSQL("use " + HiveUtils.quoteIdentifier(schema));
        try (final Statement st = conn.createStatement()) {
            ResultSet rs = doSelectSQL(st, "show table extended like " + HiveUtils.quoteIdentifier(table));
            while (rs.next()) {
                String value = rs.getString(1);
                if (value.startsWith("location:")) {
                    return value.substring(9);
                }
            }
        }
        throw new RuntimeException("Unable to identify HDFS location property of table [" + table + "]");
    }


    /**
     * Generates a sync query for inserting from a source table into the target table with no partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateSyncNonPartitionQuery(@Nonnull final String[] selectFields, @Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema,
                                                   @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        return "insert overwrite table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) +
               " select " + selectSQL +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) +
               " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);
    }

    /**
     * Generates a merge query for inserting overwriting from a source table into the target table appending to any partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param spec               the partition specification
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeWithPartitionQuery(@Nonnull final String[] selectFields, @Nonnull final PartitionSpec spec, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                     @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        return "insert into table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " + spec.toDynamicPartitionSpec() +
               " select " + selectSQL + "," + spec.toDynamicSelectSQLSpec() +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " " +
               " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);
    }

    /**
     * Produces a where clause that limits to the impacted partitions of the target table
     *
     * @param batches          a list of partition batches
     * @param useSourceColumns a boolean value to decide whether to use the source columns or target columns
     * @return a where clause sql string
     */
    private String targetPartitionsWhereClause(List<PartitionBatch> batches, boolean useSourceColumns) {
        List<String> targetPartitionsItems = new Vector<>();
        for (PartitionBatch batch : batches) {
            String column = useSourceColumns ? batch.getPartitionSpec().toSourceSQLWhere(batch.getPartitionValues())
                                             : batch.getPartitionSpec().toTargetSQLWhere(batch.getPartitionValues());
            targetPartitionsItems.add("(" + column + ")");
        }
        return (targetPartitionsItems.size() == 0 ? null : StringUtils.join(targetPartitionsItems.toArray(new String[0]), " or "));
    }

    /**
     * Generates a merge query for inserting overwriting from a source table into the target table appending to any partitions
     * uses the batch identifier processing_dttm to determine whether a new record should be inserted so only new, distinct records will be
     * inserted.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param spec               the partition specification
     * @param batches            the partitions of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeWithDedupePartitionQuery(@Nonnull final String[] selectFields, @Nonnull final PartitionSpec spec, @Nonnull final List<PartitionBatch> batches,
                                                           @Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema,
                                                           @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {

        // Strip processing_dttm for the distinct since it will always be different
        String[] distinctSelectFields = Arrays.asList(selectFields).stream().filter(v -> !("`processing_dttm`".equals(v))).collect(Collectors.toList()).toArray(new String[0]);
        final String selectAggregateSQL = StringUtils.join(distinctSelectFields, ",") + ", min(processing_dttm) processing_dttm, " + spec.toPartitionSelectSQL();
        final String groupBySQL = StringUtils.join(distinctSelectFields, ",") + "," + spec.toPartitionSelectSQL();
        final String selectSQL = StringUtils.join(selectFields, ",");
        final String targetPartitionWhereClause = targetPartitionsWhereClause(batches, false);
        final StringBuilder sb = new StringBuilder();
        sb.append("insert overwrite table ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" ")
            .append(spec.toDynamicPartitionSpec())
            .append("select ").append(selectAggregateSQL).append(" from (")
            .append(" select ").append(selectSQL).append(",").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(HiveUtils.quoteIdentifier(sourceSchema, sourceTable)).append(" ")
            .append(" where ")
            .append(" processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue))
            .append(" union all ")
            .append(" select ").append(selectSQL).append(",").append(spec.toPartitionSelectSQL())
            .append(" from ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" ");
        if (targetPartitionWhereClause != null) {
            sb.append(" where (").append(targetPartitionWhereClause).append(")");
        }
        sb.append(") t group by " + groupBySQL);

        return sb.toString();
    }




    /**
     * Generates a merge query for inserting overwriting from a source table into the target table appending to any partitions
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param spec               the partition specification
     * @param batches            the partitions of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeWithDedupePartitionQueryNoProcessingDttm(@Nonnull final String[] selectFields, @Nonnull final PartitionSpec spec, @Nonnull final List<PartitionBatch> batches,
                                                                           @Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema,
                                                                           @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        final String targetPartitionWhereClause = targetPartitionsWhereClause(batches, false);

        final StringBuilder sb = new StringBuilder();
        sb.append("insert overwrite table ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" ")
            .append(spec.toDynamicPartitionSpec())
            .append("select DISTINCT ").append(selectSQL).append(",").append(spec.toPartitionSelectSQL()).append(" from (")
            .append(" select ").append(selectSQL).append(",").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(HiveUtils.quoteIdentifier(sourceSchema, sourceTable)).append(" ")
            .append(" where ")
            .append(" processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue))
            .append(" union all ")
            .append(" select ").append(selectSQL).append(",").append(spec.toPartitionSelectSQL())
            .append(" from ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" ");
        if (targetPartitionWhereClause != null) {
            sb.append(" where (").append(targetPartitionWhereClause).append(")");
        }
        sb.append(") t");

        return sb.toString();
    }

    /**
     * Generates a dynamic partition sync query for inserting overwriting from a source table into the target table adhering to partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param spec               the partition specification or null if none
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateSyncDynamicPartitionQuery(@Nonnull final String[] selectFields, @Nonnull final PartitionSpec spec, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                       @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        return "insert overwrite table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               spec.toDynamicPartitionSpec() +
               " select " + selectSQL + "," + spec.toDynamicSelectSQLSpec() +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) +
               " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);
    }

    /**
     * Generates a query for merging from a source table into the target table with no partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeNonPartitionQueryWithDedupe(@Nonnull final String[] selectFields, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                              @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {

        String[] distinctSelectFields = Arrays.asList(selectFields).stream().filter(v -> !("`processing_dttm`".equals(v))).collect(Collectors.toList()).toArray(new String[0]);
        final String selectAggregateSQL = StringUtils.join(distinctSelectFields, ",") + ", min(processing_dttm) processing_dttm";
        final String selectSQL = StringUtils.join(selectFields, ",");
        final String groupBySQL = StringUtils.join(distinctSelectFields, ",");

        return "insert into table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               "select " + selectAggregateSQL + " from (" +
               " select " + selectSQL +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue) + " " +
               " union all " +
               " select " + selectSQL +
               " from " + HiveUtils.quoteIdentifier(targetSchema, targetTable) +
               ") x group by " + groupBySQL + " having count(processing_dttm) = 1 and min(processing_dttm) = " + HiveUtils.quoteString(feedPartitionValue);
    }

    /**
     * Generates a query for merging from a source table into the target table with no partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeNonPartitionQueryWithDedupeNoProcessingDttm(@Nonnull final String[] selectFields, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                                              @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        return "insert overwrite table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               "select " + selectSQL + " from (" +
               " select " + selectSQL +
               " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue) + " " +
               " union all " +
               " select " + selectSQL +
               " from " + HiveUtils.quoteIdentifier(targetSchema, targetTable) +
               ") x group by " + selectSQL;
    }

    /**
     * Generates a query for merging from a source table into the target table with no partitions.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceSchema       the schema or database name of the source table
     * @param sourceTable        the source table name
     * @param targetSchema       the schema or database name of the target table
     * @param targetTable        the target table name
     * @param feedPartitionValue the source processing partition value
     * @return the sql string
     */
    protected String generateMergeNonPartitionQuery(@Nonnull final String[] selectFields, @Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema,
                                                    @Nonnull final String targetTable, @Nonnull final String feedPartitionValue) {
        final String selectSQL = StringUtils.join(selectFields, ",");
        return "insert into " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               " select " + selectSQL + " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);
    }

    /**
     * Generates a query two merge two tables without partitions on a primary key.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceSchema       the name of the source table schema or database
     * @param sourceTable        the source table
     * @param targetSchema       the name of the target table schema or database
     * @param targetTable        the target table
     * @param feedPartitionValue the partition of the source table to use
     * @param columnSpecs        the column specifications
     * @return the sql
     */
    protected String generatePKMergeNonPartitionQuery(@Nonnull final String[] selectFields, @Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema,
                                                      @Nonnull final String targetTable, @Nonnull final String feedPartitionValue, @Nonnull final ColumnSpec[] columnSpecs) {

        // Include alias
        String selectSQL = StringUtils.join(selectFields, ",");
        String[] selectFieldsWithAlias = selectFieldsForAlias(selectFields, "a");
        String selectSQLWithAlias = StringUtils.join(selectFieldsWithAlias, ",");

        String joinOnClause = ColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        String[] primaryKeys = ColumnSpec.toPrimaryKeys(columnSpecs);
        String anyPK = primaryKeys[0];

        String sbSourceQuery = "select " + selectSQL + " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " where processing_dttm= " + HiveUtils.quoteString(feedPartitionValue);

        // First finds all records in valid
        // Second finds all records in target that should be preserved for impacted partitions
        return "insert overwrite table " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " " +
               "select " + selectSQL + " from (" +
               "  select " + selectSQL +
               "  from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " a" +
               "  where " +
               "  a.processing_dttm = " + HiveUtils.quoteString(feedPartitionValue) +
               " union all" +
               "  select " + selectSQLWithAlias +
               "  from " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " a left outer join (" + sbSourceQuery + ") b " +
               "  on (" + joinOnClause + ")" +
               "  where " +
               "  (b." + anyPK + " is null)) t";
    }

    /**
     * Generates a query two merge two tables containing partitions on a primary key.
     *
     * @param selectFields       the list of fields in the select clause of the source table
     * @param partitionSpec      partition specification
     * @param sourceSchema       the name of the source table schema or database
     * @param sourceTable        the source table
     * @param targetSchema       the name of the target table schema or database
     * @param targetTable        the target table
     * @param feedPartitionValue the partition of the source table to use
     * @param columnSpecs        the column specifications
     * @return the sql
     */
    protected String generatePKMergePartitionQuery(@Nonnull final String[] selectFields, @Nonnull final PartitionSpec partitionSpec, @Nonnull final String sourceSchema,
                                                   @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue,
                                                   @Nonnull final ColumnSpec[] columnSpecs) {
        // Include alias
        String selectSQL = StringUtils.join(selectFields, ",");
        String[] selectFieldsWithAlias = selectFieldsForAlias(selectFields, "a");
        String selectSQLWithAlias = StringUtils.join(selectFieldsWithAlias, ",");

        String joinOnClause = ColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        String[] primaryKeys = ColumnSpec.toPrimaryKeys(columnSpecs);
        PartitionSpec partitionSpecWithAlias = partitionSpec.newForAlias("a");
        String anyPK = primaryKeys[0];

        List<PartitionBatch> batches = createPartitionBatchesforPKMerge(partitionSpec, sourceSchema, sourceTable, targetSchema, targetTable, feedPartitionValue, joinOnClause);
        String targetPartitionWhereClause = targetPartitionsWhereClause(PartitionBatch.toPartitionBatchesForAlias(batches, "a"), false);

        // TODO: If the records matching the primary key between the source and target are in a different partition
        // AND the matching records are the only remaining records of the partition, then the following sql will fail to overwrite the
        // remaining record.  We need to detect this and then delete partition? This is a complex scenario..

        String sbSourceQuery = "select " + selectSQL + "," + partitionSpec.toDynamicSelectSQLSpec() + " from " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable)
                               + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);

        // First finds all records in valid
        // Second finds all records in target that should be preserved for impacted partitions
        StringBuilder sb = new StringBuilder();
        sb.append("insert overwrite table ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" ")
            .append(partitionSpec.toDynamicPartitionSpec())
            .append("select ").append(selectSQL).append(",").append(partitionSpec.toPartitionSelectSQL()).append(" from (")
            .append("  select ").append(selectSQLWithAlias).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(HiveUtils.quoteIdentifier(sourceSchema, sourceTable)).append(" a")
            .append("  where ")
            .append("  a.processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue))
            .append(" union all ")
            .append("  select ").append(selectSQLWithAlias).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(HiveUtils.quoteIdentifier(targetSchema, targetTable)).append(" a left outer join (").append(sbSourceQuery).append(") b ")
            .append("  on (").append(joinOnClause).append(")")
            .append("  where ")
            .append("  (b.").append(anyPK).append(" is null)");
        if (targetPartitionWhereClause != null) {
            sb.append(" and (").append(targetPartitionWhereClause).append(")");
        }
        sb.append(") t");

        return sb.toString();
    }

    /**
     * Finds all partitions that contain matching keys.
     *
     * @param spec               the partition spec
     * @param sourceSchema       the name of the source table schema or database
     * @param sourceTable        the source table
     * @param targetSchema       the name of the target table schema or database
     * @param targetTable        the target table
     * @param feedPartitionValue the partition of the source table to use
     * @param joinOnClause       the JOIN clause for the source and target tables
     * @return the matching partitions
     */
    protected List<PartitionBatch> createPartitionBatchesforPKMerge(@Nonnull final PartitionSpec spec, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                                    @Nonnull final String targetSchema, @Nonnull final String targetTable, @Nonnull final String feedPartitionValue,
                                                                    @Nonnull final String joinOnClause) {
        List<PartitionBatch> v;
        PartitionSpec aliasSpecA = spec.newForAlias("a");

        // Find all partitions that contain matching keys
        String sql = "select " + aliasSpecA.toPartitionSelectSQL() + ", count(0)" +
                     " from " + HiveUtils.quoteIdentifier(targetSchema, targetTable) + " a join " + HiveUtils.quoteIdentifier(sourceSchema, sourceTable) + " b" +
                     " on " + joinOnClause +
                     " where b.processing_dttm = '" + feedPartitionValue + "'" +
                     " group by " + aliasSpecA.toPartitionSelectSQL();
        try (final Statement st = conn.createStatement()) {
            logger.info("Selecting target partitions query [" + sql + "]");
            ResultSet rs = doSelectSQL(st, sql);
            v = toPartitionBatches(spec, rs);
        } catch (SQLException e) {
            logger.error("Failed to select partition batches SQL {} with error {}", sql, e);
            throw new RuntimeException("Failed to select partition batches", e);
        }
        return v;
    }


    protected void doExecuteSQL(String sql) {

        try (final Statement st = conn.createStatement()) {
            logger.info("Executing doExecuteSQL batch sql {}", sql);
            st.execute(sql);
        } catch (SQLException e) {
            logger.error("Failed to execute {} with error {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    protected ResultSet doSelectSQL(Statement st, String sql) throws SQLException {

        logger.info("Executing sql select {}", sql);
        return st.executeQuery(sql);
    }


    /*
    Generates batches of partitions in the source table
     */
    protected List<PartitionBatch> toPartitionBatches(PartitionSpec spec, ResultSet rs) throws SQLException {
        Vector<PartitionBatch> v = new Vector<>();
        int count = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            String[] values = new String[count];
            for (int i = 1; i <= count; i++) {
                Object oVal = rs.getObject(i);
                String sVal = (oVal == null ? "" : oVal.toString());
                values[i - 1] = StringUtils.defaultString(sVal, "");
            }
            Long numRecords = rs.getLong(count);
            v.add(new PartitionBatch(numRecords, spec, values));
        }
        logger.info("Number of partitions [" + v.size() + "]");

        return v;
    }

    /**
     * Generates batches of partitions in the source table.
     *
     * @param spec          the partition specification
     * @param sourceSchema  the schema or database name of the source table
     * @param sourceTable   the source table name
     * @param feedPartition the source processing partition value
     */
    protected List<PartitionBatch> createPartitionBatches(@Nonnull final PartitionSpec spec, @Nonnull final String sourceSchema, @Nonnull final String sourceTable,
                                                          @Nonnull final String feedPartition) {
        List<PartitionBatch> v;
        String sql = "";
        try (final Statement st = conn.createStatement()) {
            sql = spec.toDistinctSelectSQL(sourceSchema, sourceTable, feedPartition);
            logger.info("Executing batch query [" + sql + "]");
            ResultSet rs = doSelectSQL(st, sql);
            v = toPartitionBatches(spec, rs);
        } catch (SQLException e) {
            logger.error("Failed to select partition batches SQL {} with error {}", sql, e);
            throw new RuntimeException("Failed to select partition batches", e);
        }
        return v;
    }

    /**
     * Returns the list of columns that are common to both the source and target tables.
     *
     * <p>The column names are quoted and escaped for use in a SQL query.</p>
     *
     * @param sourceSchema  the name of the source table schema or database
     * @param sourceTable   the name of the source table
     * @param targetSchema  the name of the target table schema or database
     * @param targetTable   the name of the target table
     * @param partitionSpec the partition specifications, or {@code null} if none
     * @return the columns for a SELECT statement
     */
    protected String[] getSelectFields(@Nonnull final String sourceSchema, @Nonnull final String sourceTable, @Nonnull final String targetSchema, @Nonnull final String targetTable,
                                       @Nullable final PartitionSpec partitionSpec) {
        List<String> srcFields = resolveTableSchema(sourceSchema, sourceTable);
        List<String> destFields = resolveTableSchema(targetSchema, targetTable);

        // Find common fields
        destFields.retainAll(srcFields);

        // Eliminate any partition columns
        if (partitionSpec != null) {
            destFields.removeAll(partitionSpec.getKeyNames());
        }
        String[] fields = destFields.toArray(new String[0]);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = HiveUtils.quoteIdentifier(fields[i]);
        }
        return fields;
    }

    private String[] selectFieldsForAlias(String[] selectFields, String alias) {
        return Arrays.stream(selectFields).map(s -> alias + "." + s).toArray(String[]::new);
    }

    /**
     * Retrieves the schema of the specified table.
     *
     * @param schema the database name
     * @param table  the table name
     * @return the list of columns
     */
    protected List<String> resolveTableSchema(@Nonnull final String schema, @Nonnull final String table) {

        List<String> columnSet = new Vector<>();
        try (final Statement st = conn.createStatement()) {
            // Use default database to resolve ambiguity between schema.table and table.column
            // https://issues.apache.org/jira/browse/HIVE-12184
            st.execute("use default");
            String ddl = "desc " + HiveUtils.quoteIdentifier(schema, table);
            logger.info("Resolving table schema [{}]", ddl);
            ResultSet rs = doSelectSQL(st, ddl);
            while (rs.next()) {
                // First blank row is start of partition info
                if (StringUtils.isEmpty(rs.getString(1))) {
                    break;
                }
                columnSet.add(rs.getString(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to inspect schema", e);
        }
        return columnSet;
    }


}
