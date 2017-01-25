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

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;


/**
 * Create a row from a csv format of string label,long count
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class FrequencyCSVToRow implements Function<String, Row>{

	@Override
	public Row call(String line) throws Exception {
		String[] fields = line.split(",");
		return RowFactory.create(fields[0].trim(), Long.valueOf(fields[1].trim()));
	}

}
