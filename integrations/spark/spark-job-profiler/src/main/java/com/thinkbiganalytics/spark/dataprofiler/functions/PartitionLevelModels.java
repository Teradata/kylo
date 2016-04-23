package com.thinkbiganalytics.spark.dataprofiler.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.types.StructField;

import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

import scala.Tuple2;


/**
 * Get partition-level statistics models
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class PartitionLevelModels implements FlatMapFunction<Iterator<Tuple2<Tuple2<Integer, Object>, Integer>>, StatisticsModel>{

	Map<Integer, StructField> schemaMap = new HashMap<Integer, StructField>();
	
	
	public PartitionLevelModels(Broadcast<Map<Integer, StructField>> bSchemaMap) {
		schemaMap = bSchemaMap.value();
	}
	
	
	public Iterable<StatisticsModel> call(Iterator<Tuple2<Tuple2<Integer, Object>, Integer>> iter) 
			throws Exception {

		StatisticsModel statisticsModel = new StatisticsModel();

		while (iter.hasNext()) {
			Tuple2<Tuple2<Integer, Object>, Integer> item = iter.next();
			Integer columnIndex = item._1._1;
			Object columnValue = item._1()._2;
			Long count = new Long(item._2);
			statisticsModel.add(columnIndex,  columnValue,  count, schemaMap.get(columnIndex));
		}

		List<StatisticsModel> listStatisticsModels = new ArrayList<StatisticsModel>();
		listStatisticsModels.add(statisticsModel);
		return listStatisticsModels;
	}

}
