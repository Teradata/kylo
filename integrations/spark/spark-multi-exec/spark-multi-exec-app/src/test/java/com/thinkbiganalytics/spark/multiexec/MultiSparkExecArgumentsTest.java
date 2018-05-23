/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

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

import com.thinkbiganalytics.spark.multiexec.MultiSparkExecArguments;

import org.junit.Test;

import java.util.List;

/**
 *
 */
public class MultiSparkExecArgumentsTest {
    
    private static final String[] SPARK_APP_POS_ARGS = 
                {"field1|type1|formula(column1),field2|type2|formula(column2)", 
                 "column1|type1|comment|1|1|0", 
                 "source.table", 
                 "target.table", 
                 "123", 
                 "dedupe_and_merge"};
    private static final String[] SPARK_APP_SHORT_ARGS = 
                {"-p", "field1|type1|formula(column1),field2|type2|formula(column2)", 
                 "-c", "column1|type1|comment|1|1|0", 
                 "-s", "source.table", 
                 "-t", "target.table", 
                 "-v", "123", 
                 "-m", "dedupe_and_merge"};
    private static final String[] SPARK_APP_LONG_ARGS = 
                {"--partition-keys", "field1|type1|formula(column1),field2|type2|formula(column2)", 
                 "--column-specs", "column1|type1|comment|1|1|0", 
                 "--source-table", "source.table", 
                 "--target-table", "target.table", 
                 "--partition-value", "123", 
                 "--merge-strategy", "DEDUPE_AND_MERGE"};
    
    @Test
    public void testCreateCommandLine() {
        List<SparkApplicationCommand> commands = new SparkApplicationCommandsBuilder()
                        .application("merge")
                            .className("com.thinkbiganalytics.spark.mergetable.TableMergeApp")
                            .addArgument("partition-keys", "\"registration_dttm_month|int|month(registration_dttm) registration_dttm_year|int|year(registration_dttm)\"")
                            .addArgument("column-specs", "\"registration_dttm|timestamp||0|1|0|registration_dttm id|int||1|0|0|id first_name|string||0|0|0|first_name last_name|string||0|0|0|last_name email|string||0|0|0|email gender|string||0|0|0|gender\"") 
                            .addArgument("source-table", "spark_merge.user_ingest_sparksync_valid")
                            .addArgument("target-table", "spark_merge.user_ingest_sparksync")
                            .addArgument("partition-value", "1523397222536")
                            .addArgument("merge-strategy", "DEDUPE_AND_MERGE")
                            .add()
                        .build();
        
        String[] commandLine = MultiSparkExecArguments.createCommandLine(commands);
//        System.out.printf("%s '%s'", commandLine[0], commandLine[1]);
        
        assertThat(commandLine).isNotEmpty();
    }
    
}
