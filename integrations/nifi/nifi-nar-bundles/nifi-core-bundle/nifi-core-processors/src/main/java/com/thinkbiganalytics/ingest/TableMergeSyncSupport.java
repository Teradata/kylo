/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.ingest;

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

/**
 * Merge or Sync from a table into a target table. Dedupes and uses partition strategy of the target table. Sync will completely replace the target table with the contents from the source.  Merge will
 * append the data into the target table adhering to partitions if defined.  If Dedupe is specified then duplicates will be stripped.
 */
public class TableMergeSyncSupport implements Serializable {

    public static Logger logger = LoggerFactory.getLogger(TableMergeSyncSupport.class);

    protected Connection conn;

    public TableMergeSyncSupport(Connection conn) {
        Validate.notNull(conn);
        this.conn = conn;
    }

    public void enableDynamicPartitions() {
        doExecuteSQL("set hive.exec.dynamic.partition=true");
        doExecuteSQL("set hive.exec.dynamic.partition.mode=nonstrict");
    }

    /**
     * Performs a sync replacing all data in the target table. A temporary table is created with the new data, old table dropped and the temporary table renamed to become the new table.  This causes a
     * very brief lapse for consumers between when the table is dropped and the rename
     *
     * @param sourceTable        the source table
     * @param fqTargetTable      the fully qualified target table name
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     */
    public void doSync(String sourceTable, String fqTargetTable, PartitionSpec partitionSpec, String feedPartitionValue) throws SQLException {

        Validate.notEmpty(sourceTable);
        Validate.notEmpty(fqTargetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);
        Validate.isTrue(fqTargetTable.contains("."), "Expecting qualified table name schema.table");

        // Extract schema from fully qualified table
        String[] schemaPart = fqTargetTable.split("\\.");
        String schema = schemaPart[0];
        String targetTable = schemaPart[1];

        // Extract the existing HDFS location of data
        String refTableLocation = extractTableLocation(schema, targetTable);

        // 1. Create a temporary "sync" table for storing our latest snapshot
        String syncTableLocation = deriveSyncTableLocation(targetTable, refTableLocation);
        String syncTable = createSyncTable(fqTargetTable, syncTableLocation);

        // 2. Populate the temporary "sync" table
        String[] selectFields = getSelectFields(sourceTable, syncTable, partitionSpec);
        String syncSQL;
        if (partitionSpec.isNonPartitioned()) {
            syncSQL = generateSyncNonPartitionQuery(selectFields, sourceTable, syncTable, feedPartitionValue);
        } else {
            syncSQL = generateSyncDynamicPartitionQuery(selectFields, partitionSpec, sourceTable, syncTable, feedPartitionValue);
        }
        doExecuteSQL(syncSQL);

        // 3. Drop the sync table. Since it is a managed table it will drop the old data
        dropTable(fqTargetTable);

        // 4. Rename the sync table
        renameTable(syncTable, fqTargetTable);
    }

    /**
     * Performs the doMerge and insert into the target table from the source table
     *
     * @param sourceTable        the source table
     * @param targetTable        the target table
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     * @param shouldDedupe       whether to perform dedupe during merge
     */
    public List<PartitionBatch> doMerge(String sourceTable, String targetTable, PartitionSpec partitionSpec, String feedPartitionValue, boolean shouldDedupe) {

        List<PartitionBatch> batches = null;

        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);

        String[] selectFields = getSelectFields(sourceTable, targetTable, partitionSpec);
        String sql = null;
        if (partitionSpec.isNonPartitioned()) {
            if (shouldDedupe) {
                sql = generateMergeNonPartitionQueryWithDedupe(selectFields, sourceTable, targetTable, feedPartitionValue);
            } else {
                sql = generateMergeNonPartitionQuery(selectFields, sourceTable, targetTable, feedPartitionValue);
            }
        } else {
            if (shouldDedupe) {
                batches = createPartitionBatches(partitionSpec, sourceTable, feedPartitionValue);
                if (batches.size() > 0) {
                    sql = generateMergeWithDedupePartitionQuery(selectFields, partitionSpec, batches, sourceTable, targetTable, feedPartitionValue);
                }
            } else {
                sql = generateMergeWithPartitionQuery(selectFields, partitionSpec, sourceTable, targetTable, feedPartitionValue);
            }
        }
        doExecuteSQL(sql);
        return batches;
    }

    /**
     * Updates any rows matching the same primary key, otherwise inserts the value into the appropriate partition
     *
     * @param sourceTable        the source table
     * @param targetTable        the target table
     * @param partitionSpec      the partition specification
     * @param feedPartitionValue the source processing partition value
     */
    public void doPKMerge(String sourceTable, String targetTable, PartitionSpec partitionSpec, String feedPartitionValue, ColumnSpec[] columnSpecs) {

        Validate.notEmpty(sourceTable);
        Validate.notEmpty(targetTable);
        Validate.notNull(partitionSpec);
        Validate.notNull(feedPartitionValue);

        String[] selectFields = getSelectFields(sourceTable, targetTable, partitionSpec);
        String sql = null;
        if (partitionSpec.isNonPartitioned()) {
            sql = generatePKMergeNonPartitionQuery(selectFields, sourceTable, targetTable, feedPartitionValue, columnSpecs);
        } else {
            sql = generatePKMergePartitionQuery(selectFields, partitionSpec, createPartitionBatches(partitionSpec, sourceTable, feedPartitionValue), sourceTable, targetTable, feedPartitionValue,
                                                columnSpecs);
        }
        doExecuteSQL(sql);
    }

    /**
     * Create a new table like the old table with the new location
     *
     * @param table the name of the reference table
     * @return the new HDFS location
     */

    private String createSyncTable(String table, String syncTableLocation) throws SQLException {

        String syncTable = table + "_" + System.currentTimeMillis();
        String createSQL = "create external table " + syncTable + " like " + table + " location '" + syncTableLocation + "'";
        doExecuteSQL(createSQL);
        return syncTable;
    }

    /**
     * Drop table removing the data
     */
    public void dropTable(String table) {
        // Make managed to remove the old data
        String makeManagedSQL = "alter table " + table + " SET TBLPROPERTIES ('EXTERNAL'='FALSE')";
        doExecuteSQL(makeManagedSQL);
        String sql = "DROP TABLE " + table;
        doExecuteSQL(sql);
    }

    /**
     * Drop table removing the data
     */
    public void renameTable(String oldName, String newName) {
        String sql = "alter table " + oldName + " RENAME TO " + newName;
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
     * Extract the HDFS location of the table data
     *
     * @param table the table data
     */
    private String extractTableLocation(String schema, String table) throws SQLException {
        doExecuteSQL("use " + schema);
        try (final Statement st = conn.createStatement()) {
            ResultSet rs = doSelectSQL(st, "show table extended like " + table);
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
     * Generates a sync query for inserting from a source table into the target table with no partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateSyncNonPartitionQuery(String[] selectFields, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");

        StringBuilder sb = new StringBuilder();
        sb.append("insert overwrite table ").append(targetTable).append(" ").append(" select ").append(selectSQL)
            .append(" from ").append(sourceTable).append(" where processing_dttm='" + feedPartitionValue + "' ");

        return sb.toString();
    }

    /**
     * Generates a merge query for inserting overwriting from a source table into the target table appending to any partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param spec         the partition specification or null if none
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateMergeWithPartitionQuery(String[] selectFields, PartitionSpec spec, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");

        StringBuilder sb = new StringBuilder();
        sb.append("insert into table ").append(targetTable).append(" ")
            .append(spec.toDynamicPartitionSpec())
            .append(" select ").append(selectSQL).append(",").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(sourceTable).append(" ")
            .append(" where ")
            .append(" processing_dttm='").append(feedPartitionValue).append("'");

        return sb.toString();
    }

    /*
    Produces a where clause that limits to the impacted partitions of the target table
     */
    private String targetPartitionsWhereClause(List<PartitionBatch> batches) {
        List<String> targetPartitionsItems = new Vector<>();
        for (PartitionBatch batch : batches) {
            targetPartitionsItems.add("(" + batch.getPartitionSpec().toTargetSQLWhere(batch.getPartionValues()) + ")");
        }
        return StringUtils.join(targetPartitionsItems.toArray(new String[0]), " or ");
    }

    /**
     * Generates a merge query for inserting overwriting from a source table into the target table appending to any partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param spec         the partition specification or null if none
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateMergeWithDedupePartitionQuery(String[] selectFields, PartitionSpec spec, List<PartitionBatch> batches, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");
        StringBuffer sb = new StringBuffer();
        String targetPartitionWhereClause = targetPartitionsWhereClause(batches);

        sb.append("insert overwrite table ").append(targetTable).append(" ")
            .append(spec.toDynamicPartitionSpec())
            .append("select DISTINCT ").append(selectSQL).append(",").append(spec.toPartitionSelectSQL()).append(" from (")
            .append(" select ").append(selectSQL).append(",").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(sourceTable).append(" ")
            .append(" where ")
            .append(" processing_dttm='").append(feedPartitionValue).append("'")
            .append(" union all ")
            .append(" select ").append(selectSQL).append(",").append(spec.toPartitionSelectSQL())
            .append(" from ").append(targetTable).append(" ")
            .append(" where ").append(targetPartitionWhereClause).append(") t");

        return sb.toString();
    }

    /**
     * Generates a dynamic partition sync query for inserting overwriting from a source table into the target table adhering to partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param spec         the partition specification or null if none
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateSyncDynamicPartitionQuery(String[] selectFields, PartitionSpec spec, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");

        StringBuffer sb = new StringBuffer();
        sb.append("insert overwrite table ").append(targetTable).append(" ")
            .append(spec.toDynamicPartitionSpec());

        sb.append(" select ").append(selectSQL).append(",").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(sourceTable).append(" ")
            .append(" where ")
            .append(" processing_dttm='" + feedPartitionValue + "'");

        return sb.toString();
    }

    /**
     * Generates a query for merging from a source table into the target table with no partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateMergeNonPartitionQueryWithDedupe(String[] selectFields, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");

        StringBuffer sb = new StringBuffer();
        sb.append("insert overwrite table ")
            .append(targetTable).append(" ").append(" select ").append(selectSQL).append(" from (")
            .append(" select ").append(selectSQL)
            .append(" from ").append(sourceTable).append(" where processing_dttm='" + feedPartitionValue + "' ")
            .append(" union all ")
            .append(" select ").append(selectSQL)
            .append(" from ").append(targetTable)
            .append(") x group by ").append(selectSQL);

        return sb.toString();
    }

    /**
     * Generates a query for merging from a source table into the target table with no partitions
     *
     * @param selectFields the list of fields in the select clause of the source table
     * @param sourceTable  the source table
     * @param targetTable  the target table
     * @return the sql string
     */
    protected String generateMergeNonPartitionQuery(String[] selectFields, String sourceTable, String targetTable, String feedPartitionValue) {

        String selectSQL = StringUtils.join(selectFields, ",");

        StringBuffer sb = new StringBuffer();
        sb.append("insert into ");
        sb.append(targetTable).append(" ");
        sb.append(" select ").append(selectSQL)
            .append(" from ").append(sourceTable).append(" where processing_dttm='" + feedPartitionValue + "' ");

        return sb.toString();
    }

    /**
     * @param selectFields       the list of fields in the select clause of the source table
     * @param sourceTable        the source table
     * @param targetTable        the target table
     * @param feedPartitionValue the partition of the source table to use
     * @param columnSpecs        the column specifications
     * @return the sql
     */
    protected String generatePKMergeNonPartitionQuery(String[] selectFields, String sourceTable, String
        targetTable, String feedPartitionValue, ColumnSpec[] columnSpecs) {

        // Include alias
        String selectSQL = StringUtils.join(selectFields, ",");
        String[] selectFieldsWithAlias = selectFieldsForAlias(selectFields, "a");
        String selectSQLWithAlias = StringUtils.join(selectFieldsWithAlias, ",");

        String joinOnClause = ColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        String[] primaryKeys = ColumnSpec.toPrimaryKeys(columnSpecs);
        String anyPK = primaryKeys[0];

        StringBuffer sbSourceQuery = new StringBuffer();
        sbSourceQuery.append("select ").append(selectSQL).append(" from "+sourceTable)
            .append(" where processing_dttm='").append(feedPartitionValue).append("'");

        StringBuffer sb = new StringBuffer();

        // First finds all records in valid
        // Second finds all records in target that should be preserved for impacted partitions
        sb.append("insert overwrite table ").append(targetTable).append(" ")
            .append("select ").append(selectSQL).append(" from (")
            .append("  select ").append(selectSQL)
            .append("  from ").append(sourceTable).append(" a")
            .append("  where ")
            .append("  a.processing_dttm='").append(feedPartitionValue).append("'")
            .append(" union all ")
            .append("  select ").append(selectSQLWithAlias)
            .append("  from ").append(targetTable).append(" a left outer join (").append(sbSourceQuery.toString()).append(") b ")
            .append("  on (").append(joinOnClause).append(")")
            .append("  where ")
            .append("  (b.").append(anyPK).append(" is null)) t");

        return sb.toString();
    }

    /**
     * @param selectFields       the list of fields in the select clause of the source table
     * @param partitionSpec      partition specification
     * @param batches            the list of partitions impacted by the merge
     * @param sourceTable        the source table
     * @param targetTable        the target table
     * @param feedPartitionValue the partition of the source table to use
     * @param columnSpecs        the column specifications
     * @return the sql
     */
    protected String generatePKMergePartitionQuery(String[] selectFields, PartitionSpec partitionSpec, List<PartitionBatch> batches, String sourceTable, String
        targetTable, String feedPartitionValue, ColumnSpec[] columnSpecs) {

        // Include alias
        String selectSQL = StringUtils.join(selectFields, ",");
        String[] selectFieldsWithAlias = selectFieldsForAlias(selectFields, "a");
        String selectSQLWithAlias = StringUtils.join(selectFieldsWithAlias, ",");

        String joinOnClause = ColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        String[] primaryKeys = ColumnSpec.toPrimaryKeys(columnSpecs);
        PartitionSpec partitionSpecWithAlias = partitionSpec.newForAlias("a");
        String targetPartitionWhereClause = targetPartitionsWhereClause(PartitionBatch.toPartitionBatchesForAlias(batches, "a"));
        String anyPK = primaryKeys[0];

        StringBuffer sbSourceQuery = new StringBuffer();
        sbSourceQuery.append("select ").append(selectSQL).append(",").append(partitionSpec.toDynamicSelectSQLSpec()).append(" from "+sourceTable)
            .append(" where processing_dttm='").append(feedPartitionValue).append("'");

        StringBuffer sb = new StringBuffer();

        // First finds all records in valid
        // Second finds all records in target that should be preserved for impacted partitions
        sb.append("insert overwrite table ").append(targetTable).append(" ")
            .append(partitionSpec.toDynamicPartitionSpec())
            .append("select ").append(selectSQL).append(",").append(partitionSpec.toPartitionSelectSQL()).append(" from (")
            .append("  select ").append(selectSQLWithAlias).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(sourceTable).append(" a")
            .append("  where ")
            .append("  a.processing_dttm='").append(feedPartitionValue).append("'")
            .append(" union all ")
            .append("  select ").append(selectSQLWithAlias).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(targetTable).append(" a left outer join (").append(sbSourceQuery.toString()).append(") b ")
            .append("  on (").append(joinOnClause).append(")")
            .append("  where ")
            .append("  (b.").append(anyPK).append(" is null)")
            .append("  and (").append(targetPartitionWhereClause).append(")) t");

        return sb.toString();
    }


    protected void doExecuteSQL(String sql) {

        try (final Statement st = conn.createStatement()) {
            logger.info("Executing doMerge batch sql {}", sql);
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


    /*
    Generates batches of partitions in the source table
     */
    protected List<PartitionBatch> createPartitionBatches(PartitionSpec spec, String sourceTable, String feedPartition) {
        List<PartitionBatch> v;
        String sql = "";
        try (final Statement st = conn.createStatement()) {
            sql = spec.toDistinctSelectSQL(sourceTable, feedPartition);
            logger.info("Executing batch query [" + sql + "]");
            ResultSet rs = doSelectSQL(st, sql);
            v = toPartitionBatches(spec, rs);
        } catch (SQLException e) {
            logger.error("Failed to select partition batches SQL {} with error {}", sql, e);
            throw new RuntimeException("Failed to select partition batches", e);
        }
        return v;
    }


    protected String[] getSelectFields(String sourceTable, String destTable, PartitionSpec partitionSpec) {
        List<String> srcFields = resolveTableSchema(sourceTable);
        List<String> destFields = resolveTableSchema(destTable);

        // Find common fields
        destFields.retainAll(srcFields);

        // Eliminate any partition columns
        if (partitionSpec != null) {
            destFields.removeAll(partitionSpec.getKeyNames());
        }
        String[] fields = destFields.toArray(new String[0]);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = escaped(fields[i]);
        }
        return fields;
    }

    private String escaped(String item) {
        return (!item.startsWith("`") ? "`" + item + "`" : item);
    }

    private String[] selectFieldsForAlias(String[] selectFields, String alias) {
        return Arrays.stream(selectFields).map(s -> alias + "." + s).toArray(size -> new String[size]);
    }

    protected List<String> resolveTableSchema(String qualifiedTablename) {

        List<String> columnSet = new Vector<>();
        try (final Statement st = conn.createStatement()) {
            // Use default database to resolve ambiguity between schema.table and table.column
            // https://issues.apache.org/jira/browse/HIVE-12184
            st.execute("use default");
            String ddl = "desc " + qualifiedTablename;
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
