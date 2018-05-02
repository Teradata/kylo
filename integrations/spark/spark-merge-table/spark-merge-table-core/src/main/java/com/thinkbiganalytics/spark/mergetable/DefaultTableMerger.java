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
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Default implementation of a TableMerger.
 */
public class DefaultTableMerger implements TableMerger {

    private final SparkContextService scs;

    public DefaultTableMerger(SparkContextService scs) {
        this.scs = scs;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMerger#merge(org.apache.spark.sql.hive.HiveContext, com.thinkbiganalytics.spark.mergetable.TableMergeConfig, java.lang.String, boolean)
     */
    @Override
    public void merge(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue, boolean shouldDedupe) {
        Validate.notNull(context);
        Validate.notNull(mergeConfig);
        Validate.notEmpty(feedPartitionValue);

        final List<String> selectFields = getSelectFields(context, mergeConfig);
        final String sql;
        
        if (mergeConfig.getPartionSpec().isNonPartitioned()) {
            if (shouldDedupe) {
                sql = generateMergeNonPartitionQueryWithDedupe(selectFields, mergeConfig, feedPartitionValue);
            } else {
                sql = generateMergeNonPartitionQuery(selectFields, mergeConfig, feedPartitionValue);
            }
        } else {
            if (shouldDedupe) {
                sql = generateMergeWithDedupePartitionQuery(context, selectFields, mergeConfig, feedPartitionValue);
            } else {
                sql = generateMergeWithPartitionQuery(selectFields, mergeConfig, feedPartitionValue);
            }
        }
        
        executeSQL(context, sql);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMerger#mergeOnPrimaryKey(org.apache.spark.sql.hive.HiveContext, com.thinkbiganalytics.spark.mergetable.TableMergeConfig, java.lang.String, com.thinkbiganalytics.spark.mergetable.ColumnSpec[])
     */
    @Override
    public void mergeOnPrimaryKey(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue) {
        Validate.notNull(context);
        Validate.notNull(mergeConfig);
        Validate.notEmpty(feedPartitionValue);
        Validate.notEmpty(mergeConfig.getColumnSpecs());

        final List<String> selectFields = getSelectFields(context, mergeConfig);
        final String sql = mergeConfig.getPartionSpec().isNonPartitioned()
                        ? generatePkMergeNonPartionQuery(context, selectFields, mergeConfig, feedPartitionValue, mergeConfig.getColumnSpecs())
                        : generatePkMergePartionQuery(context, selectFields, mergeConfig, feedPartitionValue, mergeConfig.getColumnSpecs());
        executeSQL(context, sql);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMerger#synchronize(org.apache.spark.sql.hive.HiveContext, com.thinkbiganalytics.spark.mergetable.TableMergeConfig, java.lang.String, boolean)
     */
    @Override
    public void synchronize(HiveContext context, TableMergeConfig mergeConfig, String feedPartitionValue, boolean rollingSync) {
        final StringBuilder sql = new StringBuilder();
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final List<String> selectFields = getSelectFields(context, mergeConfig);
        final String selectCols = StringUtils.join(selectFields, ",");
        final PartitionSpec partitionSpec = mergeConfig.getPartionSpec();
        
        sql.append("insert overwrite table ").append(targetTable).append(" ");
        
        if (rollingSync || ! partitionSpec.isNonPartitioned()) {
            sql.append(partitionSpec.toDynamicPartitionSpec()).append(" ")
                .append("select ").append(selectCols).append(", ").append(partitionSpec.toDynamicSelectSQLSpec()).append(" ");
        } else {
            sql.append("select ").append(selectCols).append(" ");
        }
        
        sql.append("from ").append(srcTable).append(" ")
            .append("where processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue)).append(" ");
        
        if (rollingSync) {
            List<PartitionBatch> batches = createPartitionBatches(context, mergeConfig, feedPartitionValue);
            
            sql.append(" and (").append(targetPartitionsWhereClause(batches, true)).append(")");
        }
        
        executeSQL(context, sql.toString());
    }

    private String generatePkMergeNonPartionQuery(HiveContext context, List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue, List<ColumnSpec> columnSpecs) {
        final StringBuilder sql = new StringBuilder();
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final String selectCols = StringUtils.join(selectFields, ",");
        final String selectAliasCols = StringUtils.join(selectFieldsForAlias(selectFields, "a"), ",");

        final String joinOnClause = DefaultColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        final List<String> primaryKeys = DefaultColumnSpec.toPrimaryKeys(columnSpecs);
        final String anyPK = primaryKeys.get(0);

        String sbSourceQuery = "select " + selectCols + " from " + srcTable + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);

        sql.append("insert overwrite table ").append(targetTable).append(" ")
            .append("select ").append(selectCols).append(" from (")
            .append("  select ").append(selectCols)
            .append("  from ").append(srcTable).append(" a")
            .append("  where ")
            .append("  a.processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue))
            .append(" union all ")
            .append("  select ").append(selectAliasCols)
            .append("  from ").append(targetTable).append(" a left outer join (").append(sbSourceQuery).append(") b ")
            .append("  on (").append(joinOnClause).append(")")
            .append("  where ")
            .append("  (b.").append(anyPK).append(" is null)) t");

        return sql.toString();
    }

    private String generatePkMergePartionQuery(HiveContext context, List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue, List<ColumnSpec> columnSpecs) {
        final StringBuilder sql = new StringBuilder();
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final String selectCols = StringUtils.join(selectFields, ",");
        final String selectAliasCols = StringUtils.join(selectFieldsForAlias(selectFields, "a"), ",");

        final String joinOnClause = DefaultColumnSpec.toPrimaryKeyJoinSQL(columnSpecs, "a", "b");
        final List<String> primaryKeys = DefaultColumnSpec.toPrimaryKeys(columnSpecs);
        final PartitionSpec partitionSpec = mergeConfig.getPartionSpec();
        final PartitionSpec partitionSpecWithAlias = mergeConfig.getPartionSpec().withAlias("a");
        final String anyPK = primaryKeys.get(0);

        List<PartitionBatch> batches = createPkPartitionBatches(context, mergeConfig, feedPartitionValue, joinOnClause);
        String targetPartitionWhereClause = targetPartitionsWhereClause(PartitionBatch.toPartitionBatchesForAlias(batches, "a"), false);
        
        String sbSourceQuery = "select " + selectCols + "," + partitionSpec.toDynamicSelectSQLSpec() + " from " + srcTable
                        + " where processing_dttm = " + HiveUtils.quoteString(feedPartitionValue);


        sql.append("insert overwrite table ").append(targetTable).append(" ")
            .append(partitionSpec.toDynamicPartitionSpec()).append(" ")
            .append("select ").append(selectCols).append(",").append(partitionSpec.toPartitionSelectSQL()).append(" from (")
            .append("  select ").append(selectAliasCols).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(srcTable).append(" a")
            .append("  where ")
            .append("  a.processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue))
            .append(" union all ")
            .append("  select ").append(selectAliasCols).append(",").append(partitionSpecWithAlias.toDynamicSelectSQLSpec())
            .append("  from ").append(targetTable).append(" a left outer join (").append(sbSourceQuery).append(") b ")
            .append("  on (").append(joinOnClause).append(")")
            .append("  where ")
            .append("  (b.").append(anyPK).append(" is null)");
        if (targetPartitionWhereClause != null) {
            sql.append(" and (").append(targetPartitionWhereClause).append(")");
        }
        sql.append(") t");

        return sql.toString();
    }

    private String generateMergeNonPartitionQueryWithDedupe(List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue) {
        final StringBuilder sql = new StringBuilder();
        final String selectCols = StringUtils.join(selectFields, ",");
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final boolean isProcessingDttm = hasProcessingDttm(selectFields);
        final String partitionVal = HiveUtils.quoteString(feedPartitionValue);
        String aggregateCols;
        String groupByCols;
        
        if (isProcessingDttm) {
            final List<String> distinctFields = new ArrayList<>(selectFields);
            distinctFields.remove("`processing_dttm`");
            groupByCols = StringUtils.join(distinctFields, ",");
            aggregateCols = groupByCols + ", min(processing_dttm) processing_dttm";
        } else {
            aggregateCols = selectCols;
            groupByCols = selectCols;
        }
        
        sql.append("insert").append(isProcessingDttm ? " into " : " overwrite ").append("table ").append(targetTable).append(" ")
            .append("select ").append(aggregateCols).append(" ")
            .append("from (")
            .append("select ").append(isProcessingDttm ? "distinct " : "").append(selectCols).append(" ")
            .append(" from ").append(srcTable)
            .append(" where processing_dttm = ").append(partitionVal)
            .append(" union all ")
            .append(" select ").append(selectCols)
            .append(" from ").append(targetTable)
            .append(") x group by ").append(groupByCols);
        
        if (isProcessingDttm) {
            sql.append(" having count(processing_dttm) = 1 and min(processing_dttm) = ").append(partitionVal);
        }
        
        return sql.toString();
    }

    private String generateMergeNonPartitionQuery(List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue) {
        final StringBuilder sql = new StringBuilder();
        final String selectCols = StringUtils.join(selectFields, ",");
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        
        sql.append("insert into ").append(targetTable).append(" ")
            .append("select ").append(selectCols).append(" from ").append(srcTable).append(" ")
            .append("where processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue));

        return sql.toString();
    }

    private String generateMergeWithDedupePartitionQuery(HiveContext context, List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue) {
        final StringBuilder sql = new StringBuilder();
        final String selectCols = StringUtils.join(selectFields, ",");
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final List<PartitionBatch> batches = createPartitionBatches(context, mergeConfig, feedPartitionValue);
        final String targetPartitionWhereClause = targetPartitionsWhereClause(batches, false);
        final PartitionSpec spec = mergeConfig.getPartionSpec();
        final boolean isProcessingDttm = hasProcessingDttm(selectFields);
        final String partitionVal = HiveUtils.quoteString(feedPartitionValue);
        
        final List<String> distinctFields = new ArrayList<>(selectFields);
        distinctFields.remove("`processing_dttm`");
        final String distinctCols = StringUtils.join(distinctFields, ",");
        
        sql.append("insert overwrite table ").append(targetTable).append(" ")
            .append(spec.toDynamicPartitionSpec()).append(" ");
        
        if (isProcessingDttm) {
            sql.append("select ").append(distinctCols).append(", min(processing_dttm) processing_dttm, ").append(spec.toPartitionSelectSQL()).append(" from (");
        } else {
            sql.append("select distict ").append(selectCols).append(", ").append(spec.toPartitionSelectSQL()).append(" from (");
        }
        
        sql.append(" select ").append(selectCols).append(", ").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(srcTable).append(" ")
            .append(" where ")
            .append(" processing_dttm = ").append(partitionVal)
            .append(" union all ")
            .append(" select ").append(selectCols).append(",").append(spec.toPartitionSelectSQL())
            .append(" from ").append(targetTable).append(" ");
        
        if (targetPartitionWhereClause != null) {
            sql.append(" where (").append(targetPartitionWhereClause).append(")");
        }
        
        if (isProcessingDttm) {
            sql.append(") t group by ").append(distinctCols).append(", ").append(spec.toPartitionSelectSQL());
        } else {
            sql.append(") t");
        }
        
        return sql.toString();
    }

    private String generateMergeWithPartitionQuery(List<String> selectFields, TableMergeConfig mergeConfig, String feedPartitionValue) {
        final StringBuilder sql = new StringBuilder();
        final String selectCols = StringUtils.join(selectFields, ",");
        final String targetTable = HiveUtils.quoteIdentifier(mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        final PartitionSpec spec = mergeConfig.getPartionSpec();
        
        sql.append("insert into ").append(targetTable).append(" ").append(spec.toDynamicPartitionSpec()).append(" ")
            .append("select ").append(selectCols).append(", ").append(spec.toDynamicSelectSQLSpec())
            .append(" from ").append(srcTable).append(" ")
            .append("where processing_dttm = ").append(HiveUtils.quoteString(feedPartitionValue));

        return sql.toString();
    }

    private void executeSQL(HiveContext context, String sql) {
        this.scs.sql(context, sql);
    }

    private List<String> selectFieldsForAlias(List<String> selectFields, String alias) {
        List<String> aliased = new ArrayList<>(selectFields.size());
        for (String field : selectFields) {
            aliased.add(alias + "." + field);
        }
        return aliased;
    }

    private boolean hasProcessingDttm(List<String> selectFields) {
        return selectFields.contains("`processing_dttm`");
    }

    private String targetPartitionsWhereClause(List<PartitionBatch> batches, boolean useSourceColumns) {
        List<String> targetPartitionsItems = new ArrayList<>();
        for (PartitionBatch batch : batches) {
            String column = useSourceColumns ? batch.getPartitionSpec().toSourceSQLWhere(batch.getPartitionValues())
                                             : batch.getPartitionSpec().toTargetSQLWhere(batch.getPartitionValues());
            targetPartitionsItems.add("(" + column + ")");
        }
        return (targetPartitionsItems.size() == 0 ? null : StringUtils.join(targetPartitionsItems.toArray(new String[0]), " or "));
    }

    private List<String> getSelectFields(HiveContext context, TableMergeConfig mergeConfig) {
        List<String> srcFields = resolveTableSchema(context, mergeConfig.getSourceSchema(), mergeConfig.getSourceTable());
        List<String> destFields = resolveTableSchema(context, mergeConfig.getTargetSchema(), mergeConfig.getTargetTable());
        List<String> common = new ArrayList<>(destFields);
        
        // Find common fields
        common.retainAll(srcFields);

        // Eliminate any partition columns
        if (mergeConfig.getPartionSpec() != null) {
            destFields.removeAll(mergeConfig.getPartionSpec().getKeyNames());
        }
        
        for (int idx = 0; idx < common.size(); idx++) {
            String field = common.get(idx);
            common.set(idx, HiveUtils.quoteIdentifier(field));
        }
        
        return common;
    }

    private List<String> resolveTableSchema(HiveContext context, String schemaName, String tableName) {
        StructType schema = scs.toDataSet(context, HiveUtils.quoteIdentifier(schemaName, tableName)).schema();
        StructField[] fields = schema.fields();
        List<String> fieldNames = new ArrayList<>(fields.length);
        
        for (int idx = 0; idx < fields.length; idx++) {
            fieldNames.add(fields[idx].name());
        }
        
        return fieldNames;
    }

    private List<PartitionBatch> createPartitionBatches(@Nonnull final HiveContext context,
                                                          @Nonnull final TableMergeConfig config,
                                                          @Nonnull final String feedPartitionValue) {
        final String sql = config.getPartionSpec().toDistinctSelectSQL(config.getSourceSchema(), config.getSourceTable(), feedPartitionValue);
        final DataSet ds = this.scs.sql(context, sql);
        final ToBatch toBatch = new ToBatch(config);
        
        return ds.javaRDD().map(toBatch).collect();
    }

    private List<PartitionBatch> createPkPartitionBatches(HiveContext context, TableMergeConfig config, String feedPartitionValue, String joinOnClause) {
        final StringBuilder sql = new StringBuilder();
        final String targetTable = HiveUtils.quoteIdentifier(config.getTargetSchema(), config.getTargetTable());
        final String srcTable = HiveUtils.quoteIdentifier(config.getSourceSchema(), config.getSourceTable());
        final PartitionSpec aliasSpecA = config.getPartionSpec().withAlias("a");
        
        sql.append("select ").append(aliasSpecA.toPartitionSelectSQL()).append(", count(0) ")
            .append("from ").append(targetTable).append(" a join ").append(srcTable).append(" b ")
            .append("on ").append(joinOnClause)
            .append("where b.processing_dttm = '").append(feedPartitionValue).append("' ")
            .append("group by ").append(aliasSpecA.toPartitionSelectSQL());
        
        final DataSet ds = this.scs.sql(context, sql.toString());
        final ToBatch toBatch = new ToBatch(config);
        
        return ds.javaRDD().map(toBatch).collect();
    }
    
    public static class ToBatch implements Function<Row, PartitionBatch> {
        private static final long serialVersionUID = 1L;
        
        private final TableMergeConfig config;
        
        public ToBatch(TableMergeConfig config) {
            this.config = config;
        }

        @Override
        public PartitionBatch call(Row row) throws Exception {
            int colCnt = row.size();
            List<String> values = new ArrayList<>(colCnt - 1);
            
            for (int idx = 0; idx < colCnt; idx++) {
                String value = row.isNullAt(idx) ? "" : row.get(idx).toString();
                values.add(value);
            }
            
            long numRecords = row.getLong(colCnt - 1);
            return new PartitionBatch(numRecords, config.getPartionSpec(), values);
        }
    }

}
