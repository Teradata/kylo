package com.thinkbiganalytics.spark.dataprofiler.core;

/*-
 * #%L
 * thinkbig-spark-job-profiler-spark-v1
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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService16;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.hive.HiveContext;

/**
 * For Spark 1
 */
@Component
public class ProfilerSparkContextServiceV1 extends SparkContextService16 implements ProfilerSparkContextService {

    @Override
    public DataSet toDataSet(HiveContext context, JavaRDD<OutputRow> outputRowsRDD, Class<OutputRow> outputRowClass) {
        return toDataSet(context.createDataFrame(outputRowsRDD, OutputRow.class));
    }
}
