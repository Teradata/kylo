package com.thinkbiganalytics.spark.dataprofiler.functions;

/*-
 * #%L
 * thinkbig-spark-job-profiler-spark-v1
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Get ((column index, column value), 1) for each column value in a row
 * @author jagrut sharma
 *
 */
public class IndividualColumnValueCounts implements PairFlatMapFunction<Row, Tuple2<Integer, Object>, Integer> {

	public Iterable<Tuple2<Tuple2<Integer, Object>, Integer>> call(Row row) throws Exception {

		List<Tuple2<Tuple2<Integer, Object>, Integer>> retValList = new ArrayList<>();
		for (int i = 0; i < row.length(); i++) {
			Object value = row.get(i);

			Tuple2<Integer, Object> insideTuple = new Tuple2<>(i, value);
			Tuple2<Tuple2<Integer, Object>, Integer> fullTuple = new Tuple2<>(insideTuple, 1);

			retValList.add(fullTuple);
		}

		return retValList;
	}
}
