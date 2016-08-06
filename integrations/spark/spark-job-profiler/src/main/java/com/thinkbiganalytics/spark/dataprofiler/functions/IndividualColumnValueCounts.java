package com.thinkbiganalytics.spark.dataprofiler.functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Row;

import scala.Tuple2;

/**
 * Get ((column index, column value), 1) for each column value in a row
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class IndividualColumnValueCounts implements PairFlatMapFunction<Row, Tuple2<Integer, Object>, Integer> {

	public Iterable<Tuple2<Tuple2<Integer, Object>, Integer>> call(Row r) throws Exception {

		List<Tuple2<Tuple2<Integer, Object>, Integer>> retValList = new ArrayList<>();
		for (int i = 0; i < r.length(); i++) {
			Object value = r.get(i);

			Tuple2<Integer, Object> insideTuple = new Tuple2<>(i, value);
			Tuple2<Tuple2<Integer, Object>, Integer> fullTuple = new Tuple2<>(insideTuple, 1);

			retValList.add(fullTuple);
		}

		return retValList;
	}

}
