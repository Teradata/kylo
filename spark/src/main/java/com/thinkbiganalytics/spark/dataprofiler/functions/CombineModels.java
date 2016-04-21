package com.thinkbiganalytics.spark.dataprofiler.functions;

import org.apache.spark.api.java.function.Function2;

import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

/**
 * Combine statistics models for two partitions
 * @author jagrut sharma
 *
 */

@SuppressWarnings("serial")
public class CombineModels implements Function2<StatisticsModel, StatisticsModel, StatisticsModel>{

	public StatisticsModel call(StatisticsModel model1, StatisticsModel model2) throws Exception {

		model1.combine(model2);

		return model1;
	}
}
