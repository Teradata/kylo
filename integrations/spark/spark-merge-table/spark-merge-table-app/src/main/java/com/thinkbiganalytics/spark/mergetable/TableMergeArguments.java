/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/*-
 * #%L
 * kylo-spark-merge-table-app
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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.IParameterSplitter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TableMergeArguments implements TableMergeConfig, Serializable {
    
    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-t", "--target-table"}, required=true,
               description = "The target table in the form <schema>.<table>", 
               converter = TableConverter.class)
    private Table targetTable;
    
    @Parameter(names = {"-s", "--source-table"}, required=true,
               description = "The source table in the form <schema>.<table>", 
               converter = TableConverter.class)
    private Table sourceTable;
    
    @Parameter(names = {"-m", "--merge-strategy"}, required=true,
               description = "The merge strategy to use - valid values: MERGE, DEDUPE_AND_MERGE, PK_MERGE, SYNC, ROLLING_SYNC",
               converter = StrategyConverter.class)
    private MergeStrategy strategy;
    
    @Parameter(names = {"-c", "--column-specs"}, required=true,
               description = "The merge strategy to use - valid values: MERGE, DEDUPE_AND_MERGE, PK_MERGE, SYNC, ROLLING_SYNC", 
               converter = ColumnSpecConverter.class,
               splitter = WhitespaceCommaSplitter.class)
    private List<ColumnSpec> columnSpecs;
    
    @Parameter(names = {"-v", "--partition-value"}, required=true,
               description = "The merge strategy to use - valid values: MERGE, DEDUPE_AND_MERGE, PK_MERGE, SYNC, ROLLING_SYNC")
    private String partitionValue;
    
    @Parameter(names = {"-p", "--partition-keys"},
               description = "A list of whitespace and/or comma-separated partition keys in the form: <field>|<type>|<formula>,...", 
               converter = PartitionKeyConverter.class,
               splitter = WhitespaceCommaSplitter.class)
    private List<PartitionKey> partitionKeys;
    
    private PartitionSpec partitionSpec;
    
    /**
     * 
     */
    public TableMergeArguments(String... args) {
        super();
        
        new JCommander(this).parse(args);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getTargetTable()
     */
    @Override
    public String getTargetTable() {
        return this.targetTable.table;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getTargetSchema()
     */
    @Override
    public String getTargetSchema() {
        return this.targetTable.schema;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getSourceTable()
     */
    @Override
    public String getSourceTable() {
        return this.sourceTable.table;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getSourceSchema()
     */
    @Override
    public String getSourceSchema() {
        return this.sourceTable.schema;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getStrategy()
     */
    @Override
    public MergeStrategy getStrategy() {
        return this.strategy;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getColumnSpecs()
     */
    @Override
    public List<ColumnSpec> getColumnSpecs() {
        return this.columnSpecs;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.spark.mergetable.TableMergeConfig#getPartionSpec()
     */
    @Override
    public PartitionSpec getPartionSpec() {
        if (this.partitionSpec == null && this.partitionKeys != null) {
            this.partitionSpec = new DefaultPartitionSpec(this.partitionKeys);
        }

        return this.partitionSpec;
    }
    
    /**
     * @return the partitionValue
     */
    public String getPartitionValue() {
        return partitionValue;
    }
    

    public static class Table implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private final String schema;
        private final String table;
        
        public Table(String schema, String table) {
            super();
            this.schema = schema;
            this.table = table;
        }

        public String getSchema() {
            return schema;
        }

        public String getTable() {
            return table;
        }
        
        @Override
        public String toString() {
            return this.schema + "." + this.table;
        }
    }
    
    /**
     * Supports separating values by commas and/or whitespace.
     */
    public static class WhitespaceCommaSplitter implements IParameterSplitter {
        public List<String> split(String value) {
            List<String> values = new ArrayList<>();
            String cleaned = value.replaceAll("\\s+", ",");
            cleaned = cleaned.replaceAll("\\\\n", ",");
            cleaned = cleaned.replaceAll(",+", ",");
            String[] tokens = cleaned.split(",");
            
            for (String token : tokens) {
                if (StringUtils.isNotBlank(token)) {
                    values.add(token);
                }
            }
            
            return values;
        }
    }

    public static class TableConverter implements IStringConverter<Table> {
        @Override
        public Table convert(String value) {
            try {
                String[] parts = value.split("\\.");
                
                Validate.isTrue(parts.length == 2, "Unrecognized table format - must be in the form <schema>.<table>: %s", value);
                Validate.notBlank(parts[0], "Invalid schema name in: ", value);
                Validate.notBlank(parts[0], "Invalid table name in: ", value);
                
                return new Table(parts[0], parts[1]);
            } catch (IllegalArgumentException e) {
                throw new ParameterException(e);
            }
        }
    }
    
    public static class ColumnSpecConverter implements IStringConverter<ColumnSpec> {
        @Override
        public ColumnSpec convert(String value) {
            return DefaultColumnSpec.createFromString(value);
        }
    }
    
    public static class PartitionKeyConverter implements IStringConverter<PartitionKey> {
        @Override
        public PartitionKey convert(String value) {
            return DefaultPartitionKey.createFromString(value);
        }
    }
    
    public static class StrategyConverter implements IStringConverter<MergeStrategy> {
        @Override
        public MergeStrategy convert(String value) {
            return MergeStrategy.valueOf(value.toUpperCase());
        }
    }
}
