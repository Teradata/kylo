package com.thinkbiganalytics.spark.dataprofiler.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;

import com.thinkbiganalytics.spark.dataprofiler.model.SchemaInfo;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

import scala.Tuple2;


/**
 * Get partition-level statistics models
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class PartitionLevelModels implements FlatMapFunction<Iterator<Tuple2<Tuple2<Integer, Object>, Integer>>, StatisticsModel>{

	public Iterable<StatisticsModel> call(Iterator<Tuple2<Tuple2<Integer, Object>, Integer>> iter) 
			throws Exception {

		StatisticsModel statisticsModel = new StatisticsModel();

		while (iter.hasNext()) {
			Tuple2<Tuple2<Integer, Object>, Integer> item = iter.next();
			Integer columnIndex = item._1._1;
			Object columnValue = item._1()._2;
			Long count = new Long(item._2);
			statisticsModel.add(columnIndex,  columnValue,  count, SchemaInfo.schemaMap.get(columnIndex));
		}

		List<StatisticsModel> listStatisticsModels = new ArrayList<StatisticsModel>();
		listStatisticsModels.add(statisticsModel);
		return listStatisticsModels;
	}

}
