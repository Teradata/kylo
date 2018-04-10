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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.beust.jcommander.ParameterException;
import com.thinkbiganalytics.spark.mergetable.TableMergeConfig.MergeStrategy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TableMergeArgumentsTest {
    
    private static final String[] SHORT_ARGS = {"-p", "field1|type1|formula(column1),field2|type2|formula(column2)", 
                                                "-c", "column1|type1|comment|1|1|0", 
                                                "-s", "source.table", 
                                                "-t", "target.table", 
                                                "-v", "123", 
                                                "-m", "dedupe_and_merge"};
    private static final String[] LONG_ARGS = {"--partition-keys", "field1|type1|formula(column1),field2|type2|formula(column2)", 
                                               "--column-specs", "column1|type1|comment|1|1|0", 
                                               "--source-table", "source.table", 
                                               "--target-table", "target.table", 
                                               "--partition-value", "123", 
                                               "--merge-strategy", "DEDUPE_AND_MERGE"};
    
    @Test
    public void testLongArgs() {
        TableMergeArguments config = new TableMergeArguments(LONG_ARGS);
        
        assertThat(config).extracting("sourceSchema", "sourceTable").containsExactly("source", "table");
        assertThat(config).extracting("targetSchema", "targetTable").containsExactly("target", "table");
        assertThat(config.getPartitionValue()).isEqualTo("123");
        assertThat(config.getStrategy()).isEqualTo(MergeStrategy.DEDUPE_AND_MERGE);
        assertThat(config.getPartionSpec()).isNotNull();
        assertThat(config.getPartionSpec().getKeys())
            .isNotEmpty()
            .extracting("key", "type", "formula")
            .contains(tuple("field1", "type1", "formula(column1)"),
                      tuple("field2", "type2", "formula(column2)"));  
        assertThat(config.getColumnSpecs()).isNotNull();
        assertThat(config.getColumnSpecs())
            .isNotEmpty()
            .extracting("name", "comment", "dataType", "primaryKey", "createDate", "modifiedDate")
            .contains(tuple("column1", "comment", "type1", true, true, false));
    }
    
    @Test
    public void testShortArgs() {
        TableMergeArguments config = new TableMergeArguments(SHORT_ARGS);
        
        assertThat(config).extracting("sourceSchema", "sourceTable").containsExactly("source", "table");
        assertThat(config).extracting("targetSchema", "targetTable").containsExactly("target", "table");
        assertThat(config.getPartitionValue()).isEqualTo("123");
        assertThat(config.getStrategy()).isEqualTo(MergeStrategy.DEDUPE_AND_MERGE);
        assertThat(config.getPartionSpec()).isNotNull();
        assertThat(config.getPartionSpec().getKeys())
            .isNotEmpty()
            .extracting("key", "type", "formula")
            .contains(tuple("field1", "type1", "formula(column1)"),
                      tuple("field2", "type2", "formula(column2)"));  
        assertThat(config.getColumnSpecs())
            .isNotEmpty()
            .extracting("name", "comment", "dataType", "primaryKey", "createDate", "modifiedDate")
            .contains(tuple("column1", "comment", "type1", true, true, false));
    }
    
    @Test
    public void testKeysSeparators() {
        String[] args = Arrays.copyOf(SHORT_ARGS, SHORT_ARGS.length);
        List<String> argList = Arrays.asList(args);
        argList.set(1, "field1|type1|formula(column1),   field2|type2|formula(column2)\tfield3|type3|formula(column3) , field4|type4|formula(column4) \n field5|type5|formula(column5)\t\n\tfield6|type6|formula(column6)");
        
        TableMergeArguments config = new TableMergeArguments(args);
        
        assertThat(config.getPartionSpec()).isNotNull();
        assertThat(config.getPartionSpec().getKeys())
            .isNotEmpty()
            .extracting("key", "type", "formula")
            .contains(tuple("field1", "type1", "formula(column1)"),
                      tuple("field2", "type2", "formula(column2)"),
                      tuple("field3", "type3", "formula(column3)"),
                      tuple("field4", "type4", "formula(column4)"),
                      tuple("field5", "type5", "formula(column5)"),
                      tuple("field6", "type6", "formula(column6)"));  
    }
    
    @Test
    public void testColumnSeparators() {
        String[] args = Arrays.copyOf(SHORT_ARGS, SHORT_ARGS.length);
        List<String> argList = Arrays.asList(args);
        argList.set(3, "column1|type1|comment|1|1|0  , \t\ncolumn2|type2|comment|0|0|1");
        
        TableMergeArguments config = new TableMergeArguments(args);
        
        assertThat(config.getColumnSpecs())
            .isNotEmpty()
            .extracting("name", "comment", "dataType", "primaryKey", "createDate", "modifiedDate")
            .contains(tuple("column1", "comment", "type1", true, true, false),
                      tuple("column2", "comment", "type2", false, false, true));  
    }
    
    @Test
    public void testDifferentOrder() {
        TableMergeArguments config = new TableMergeArguments(SHORT_ARGS[2], SHORT_ARGS[3], SHORT_ARGS[4], SHORT_ARGS[5], SHORT_ARGS[6], SHORT_ARGS[7], 
                                                             SHORT_ARGS[0], SHORT_ARGS[1], SHORT_ARGS[10], SHORT_ARGS[11], SHORT_ARGS[8], SHORT_ARGS[9]);
        
        assertThat(config).extracting("sourceSchema", "sourceTable").containsExactly("source", "table");
        assertThat(config).extracting("targetSchema", "targetTable").containsExactly("target", "table");
        assertThat(config.getStrategy()).isEqualTo(MergeStrategy.DEDUPE_AND_MERGE);
        assertThat(config.getPartionSpec()).isNotNull();
        assertThat(config.getPartionSpec().getKeys()).hasSize(2);
    } 
    
    @Test(expected = ParameterException.class)
    public void testMissiongSchema() {
        String[] args = Arrays.copyOf(SHORT_ARGS, SHORT_ARGS.length);
        List<String> argList = Arrays.asList(args);
        argList.set(5, "tableOnly");

        new TableMergeArguments(args);
    } 
    
    @Test(expected = ParameterException.class)
    public void testMissiongTable() {
        String[] args = Arrays.copyOf(SHORT_ARGS, SHORT_ARGS.length);
        List<String> argList = Arrays.asList(args);
        argList.set(5, "schemaOnly.");
        
        new TableMergeArguments(args);
    } 
    
    @Test(expected = ParameterException.class)
    public void testExtraTableCompoents() {
        String[] args = Arrays.copyOf(SHORT_ARGS, SHORT_ARGS.length);
        List<String> argList = Arrays.asList(args);
        argList.set(5, "schema.table.bogus");
        
        new TableMergeArguments(args);
    } 

    @Test(expected = ParameterException.class)
    public void testMissingSource() {
        new TableMergeArguments(SHORT_ARGS[4], SHORT_ARGS[5], SHORT_ARGS[6], SHORT_ARGS[7]);
    } 
    
    @Test(expected = ParameterException.class)
    public void testMissingTarget() {
        new TableMergeArguments(SHORT_ARGS[2], SHORT_ARGS[3], SHORT_ARGS[6], SHORT_ARGS[7]);
    } 
    
    @Test(expected = ParameterException.class)
    public void testMissingStrategy() {
        new TableMergeArguments(SHORT_ARGS[2], SHORT_ARGS[3], SHORT_ARGS[4], SHORT_ARGS[5]);
    } 
}
