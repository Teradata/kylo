package com.thinkbiganalytics.spark.dataprofiler.core;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.hive.HiveContext;

/**
 * Created by ru186002 on 10/11/2016.
 */
public interface ProfilerSparkContextService extends SparkContextService {

    DataSet toDataSet(HiveContext context, JavaRDD<OutputRow> outputRowsRDD, Class<OutputRow> outputRowClass);

}
