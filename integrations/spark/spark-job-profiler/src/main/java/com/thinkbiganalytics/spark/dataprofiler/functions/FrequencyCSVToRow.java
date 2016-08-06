package com.thinkbiganalytics.spark.dataprofiler.functions;

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
