package com.thinkbiganalytics.spark.dataprofiler.functions;

import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;
import scala.Tuple2;

import java.util.*;


/**
 * Get partition-level statistics models
 * @author jagrut sharma
 *
 */
public class PartitionLevelModels implements
		FlatMapFunction<Iterator<Tuple2<Tuple2<Integer, Object>, Integer>>, StatisticsModel> {

	private Map<Integer, StructField> schemaMap = new HashMap<>();


	public PartitionLevelModels(Broadcast<Map<Integer, StructField>> bSchemaMap) {
		schemaMap = bSchemaMap.value();
	}


	public Iterable<StatisticsModel> call(Iterator<Tuple2<Tuple2<Integer, Object>, Integer>> iter)
			throws Exception {

		StatisticsModel statisticsModel = new StatisticsModel();

		while (iter.hasNext()) {
			Tuple2<Tuple2<Integer, Object>, Integer> item = iter.next();
			Integer columnIndex = item._1()._1();
			Object columnValue = item._1()._2();
			Long count = (long) item._2();
			statisticsModel.add(columnIndex,  columnValue,  count, schemaMap.get(columnIndex));
		}

		List<StatisticsModel> listStatisticsModels = new ArrayList<>();
		listStatisticsModels.add(statisticsModel);
		return listStatisticsModels;
	}
}
