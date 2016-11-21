package com.thinkbiganalytics.spark.dataprofiler.core;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.dataprofiler.functions.CombineModels;
import com.thinkbiganalytics.spark.dataprofiler.functions.IndividualColumnValueCounts;
import com.thinkbiganalytics.spark.dataprofiler.functions.PartitionLevelModels;
import com.thinkbiganalytics.spark.dataprofiler.functions.TotalColumnValueCounts;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.util.Map;

/**
 * Created by ru186002 on 10/11/2016.
 */
@Component
public class ProfilerStrategyV2 implements ProfilerStrategy {
    @Override
    public StatisticsModel profileStatistics(DataSet set, Broadcast<Map<Integer, StructField>> bSchemaMap) {
        JavaPairRDD<Tuple2<Integer, Object>, Integer> columnValueCounts;
        StatisticsModel profileStatisticsModel;

		/* Get ((column index, column value), count) */
        columnValueCounts = set
                .javaRDD()
                .flatMapToPair(new IndividualColumnValueCounts())
                .reduceByKey(new TotalColumnValueCounts());

		/* Generate the profile model */
        profileStatisticsModel = columnValueCounts
                .mapPartitions(new PartitionLevelModels(bSchemaMap))
                .reduce(new CombineModels());

        return profileStatisticsModel;
    }
}
