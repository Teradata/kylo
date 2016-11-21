package com.thinkbiganalytics.spark.experimental;

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.accum.MapAccumulator;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerSparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.functions.FrequencyCSVToRow;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.Map.Entry;


/**
 *
 * Demo of counting invalid values for all columns in a table/query result<br>
 * [Assumes that a null in a column makes it an invalid value]<br>
 * Custom rules can be added to define what an invalid value means for a column, depending upon use case<br><br>
 * 
 * This is a demo
 * @author jagrut sharma
 *
 */
/*
 * Example command to run:
 * 
 * spark-submit --master local[*] --class com.thinkbiganalytics.spark.experimental.InvalidStatsSummaryDemo 
 * 	target/thinkbig-spark-0.0.1-SNAPSHOT.jar profiler.people statsdb.invalidcountstats
 */
@Configuration
@ComponentScan("com.thinkbiganalytics.spark")
@EnableAutoConfiguration
public class InvalidStatsSummaryDemo {

	@Autowired
	private ProfilerSparkContextService scs;


	@SuppressWarnings("serial")
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(InvalidStatsSummaryDemo.class);
		InvalidStatsSummaryDemo app = ctx.getBean(InvalidStatsSummaryDemo.class);
		app.run(args);
	}


	private void run(String[] args) {

		/* Simple command-line argument check */
		if (args.length < 2) {
			System.out.println("Two parameters needed: <input db.table> <output db.table>");
			return;
		}
		
		String inputDBTable = args[0];
		String outputDBTable = args[1];
		
		
		/* Variables */
		SparkConf conf;
		JavaSparkContext sc;
		HiveContext hiveContext;
		String queryString;
		DataSet resultDF;
		final List<String> columnNames;
		List<StructField> fields;
		StructType invalidStatsSchema;
		
		
		/* Spark configuration */		
		conf = new SparkConf().setAppName("Invalid Stats Summary Demo");				
		sc = new JavaSparkContext(conf);
		hiveContext = new HiveContext(sc.sc());		
		hiveContext.setConf("spark.sql.dialect", "hiveql");
		
		/* Set up accumulator */
		final Accumulator<Map<String, Long>> invalidFieldCounter = sc.accumulator(new HashMap<String, Long>(), "invalidFieldCounter", new MapAccumulator());
		
		/* Select data from source table */
		queryString = "select * from " + inputDBTable;
		resultDF = scs.toDataSet(hiveContext.sql(queryString));
		
		/* Get list of column names from result */
		columnNames = new ArrayList<>();
		for (StructField field: resultDF.schema().fields()) {
			columnNames.add(field.name());
		}
		
		
		/* Process result, populate accumulator as a side-effect */
		JavaRDD<Row> rows = resultDF.javaRDD().map(new Function<Row, Row>() {

			@Override
			public Row call(Row row) throws Exception {
				Map<String, Long> invalidFieldCount = new HashMap<>();
				
				for (int i = 0; i < columnNames.size(); i++) {
					/* Custom rules can be applied to check value and consider it as invalid.
					 * Simple case here treats a null as an invalid value.
					 */
					Object value = row.get(i);
					if (value == null) {
						invalidFieldCount.put(columnNames.get(i), 1L);
					}
				}
				
				invalidFieldCounter.add(invalidFieldCount);
				
				/* 
				 * Any additional processing on the input row can be done here 
				 * Here, the identity mapping returns input row without modification
				 */
				return row;
			}
			
		});
		
		rows.count();
			
		
		/* 
		 * Create a simple schema to store result:
		 * columnname, invalidcount 
		 */
		fields = new ArrayList<>();
		fields.add(DataTypes.createStructField("columnname", DataTypes.StringType, true));
		fields.add(DataTypes.createStructField("invalidcount", DataTypes.LongType, true));
		invalidStatsSchema = DataTypes.createStructType(fields);
		
		
		/*
		 * Transform map into comma-separated strings
		 */
		List<String> invalidFieldCounterList = new ArrayList<>();
		Map<String, Long> invalidFieldCounterMap = invalidFieldCounter.value();
		
		for (Entry<String, Long> entry: invalidFieldCounterMap.entrySet()) {
			invalidFieldCounterList.add(entry.getKey() + "," + entry.getValue());
		}
		
		
		/*
		 * Create a table of columnname, invalidcount
		 */
		JavaRDD<Row> invalidFieldCounterRowsRDD = sc.parallelize(invalidFieldCounterList)
													.map(new FrequencyCSVToRow());


		DataSet invalidFieldCounterRowsDF = scs.toDataSet(hiveContext.createDataFrame(invalidFieldCounterRowsRDD, invalidStatsSchema));
		invalidFieldCounterRowsDF.registerTempTable("invalidcounttemptable");
		
		/*
		 * Create partitioned output result table if not available
		 */
		String createSQL = "CREATE TABLE IF NOT EXISTS " + outputDBTable + "\n"
				+ "(columnname STRING, invalidcount BIGINT)\n"
				+ "PARTITIONED BY (processing_dttm STRING)\n"
				+ "ROW FORMAT DELIMITED\n"
				+ "FIELDS TERMINATED BY ','\n"
				+ "STORED AS TEXTFILE";
		
		hiveContext.sql(createSQL);
		
		/*
		 * Write result to output result table in a partition
		 */
		String partitionValue = UUID.randomUUID().toString();
		
		String insertSQL = "INSERT INTO TABLE " + outputDBTable
				+ " PARTITION (processing_dttm='" + partitionValue + "')"
				+ " SELECT columnname, invalidcount FROM invalidcounttemptable";
		
		hiveContext.sql(insertSQL);
		
		
		/* User notification */
		System.out.println("Invalid stats for " + inputDBTable + " saved to Hive table: " + outputDBTable  + " (partition=" + partitionValue + ")");
		
		
		/* Clean up */
		sc.close();
	}
}
