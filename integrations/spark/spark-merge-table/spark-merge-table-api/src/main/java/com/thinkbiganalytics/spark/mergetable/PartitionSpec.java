/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/*-
 * #%L
 * kylo-spark-merge-table-api
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

import java.util.List;

/**
 *
 */
public interface PartitionSpec {

    boolean isNonPartitioned();
    
    List<PartitionKey> getKeys();
    
    List<String> getKeyNames();
    
    PartitionSpec withAlias(String alias);
    
    String toTargetSQLWhere(List<String> values);
    
    String toSourceSQLWhere(List<String> values);
    
    String toPartitionSpec(List<String> values);
    
    String toDynamicPartitionSpec();
    
    String toPartitionSelectSQL();
    
    String toDynamicSelectSQLSpec();
    
    String toDistinctSelectSQL(String sourceSchema, String sourceTable, String feedPartitionValue);
}
