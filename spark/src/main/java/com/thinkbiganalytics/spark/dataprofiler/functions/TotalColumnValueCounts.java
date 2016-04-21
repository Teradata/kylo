package com.thinkbiganalytics.spark.dataprofiler.functions;

import org.apache.spark.api.java.function.Function2;

/**
 * Get ((column index, column value), totalcount) for each column
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class TotalColumnValueCounts implements Function2<Integer, Integer, Integer>{

	public Integer call(Integer a, Integer b) throws Exception {

		return a+ b;

	}

}
