package com.thinkbiganalytics.spark.dataprofiler.functions;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Get ((column index, column value), 1) for each column value in a row
 * @author jagrut sharma
 *
 */
public class IndividualColumnValueCounts
		implements PairFlatMapFunction<Row, Tuple2<Integer, Object>, Integer> {

	@Override
	public Iterator<Tuple2<Tuple2<Integer, Object>, Integer>> call(Row row) throws Exception {
		List<Tuple2<Tuple2<Integer, Object>, Integer>> retValList = new ArrayList<>();
		for (int i = 0; i < row.length(); i++) {
			Object value = row.get(i);

			Tuple2<Integer, Object> insideTuple = new Tuple2<>(i, value);
			Tuple2<Tuple2<Integer, Object>, Integer> fullTuple = new Tuple2<>(insideTuple, 1);

			retValList.add(fullTuple);
		}
		return retValList.iterator();
	}
}
