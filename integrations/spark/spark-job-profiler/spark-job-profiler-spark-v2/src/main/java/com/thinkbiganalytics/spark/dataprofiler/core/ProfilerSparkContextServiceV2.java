package com.thinkbiganalytics.spark.dataprofiler.core;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService20;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.hive.HiveContext;
import org.springframework.stereotype.Component;

/**
 * Created by ru186002 on 10/11/2016.
 */
@Component
public class ProfilerSparkContextServiceV2 extends SparkContextService20 implements ProfilerSparkContextService {

    @Override
    public DataSet toDataSet(HiveContext context, JavaRDD<OutputRow> outputRowsRDD, Class<OutputRow> outputRowClass) {
        return toDataSet(context.createDataFrame(outputRowsRDD, OutputRow.class));
    }
}
