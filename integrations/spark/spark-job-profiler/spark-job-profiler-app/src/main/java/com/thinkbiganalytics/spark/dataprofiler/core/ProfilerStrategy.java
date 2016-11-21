package com.thinkbiganalytics.spark.dataprofiler.core;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;

import java.util.Map;

/**
 * Created by ru186002 on 10/11/2016.
 */
public interface ProfilerStrategy {

    /**
     * Profile statistics for data frame
     * @param set data frame to analyze
     * @param bSchemaMap
     * @return StatisticsModel
     */
    StatisticsModel profileStatistics(DataSet set, Broadcast<Map<Integer,StructField>> bSchemaMap);
}
