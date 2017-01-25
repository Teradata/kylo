package com.thinkbiganalytics.spark.dataprofiler.functions;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
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
