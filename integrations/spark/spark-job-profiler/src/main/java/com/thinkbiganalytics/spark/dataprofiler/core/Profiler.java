package com.thinkbiganalytics.spark.dataprofiler.core;

import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.UnsupportedColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.functions.CombineModels;
import com.thinkbiganalytics.spark.dataprofiler.functions.IndividualColumnValueCounts;
import com.thinkbiganalytics.spark.dataprofiler.functions.PartitionLevelModels;
import com.thinkbiganalytics.spark.dataprofiler.functions.TotalColumnValueCounts;
import com.thinkbiganalytics.spark.dataprofiler.model.SchemaInfo;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scala.Tuple2;

/**
 * Generate data profile statistics for a table/query, and write result to a table
 * @author jagrut sharma
 * @version 0.1
 *
 */
public class Profiler {

	/* Schema lookup table */
	private static Broadcast<Map<Integer, StructField>> bSchemaMap;

	/**
	 * Main entry point into program
	 * @param args: list of args
	 */
	public static void main(String[] args) {

		/* Variables */
		SparkConf conf;
		JavaSparkContext sc;
		HiveContext hiveContext;
		DataFrame resultDF;
		String queryString;



		/* Check command line arguments and get query to run. */
		if ((queryString = checkCommandLineArgs(args)) == null) {
			return;
		}


		/* Initialize and configure Spark */
		conf = new SparkConf().setAppName(ProfilerConfiguration.APP_NAME);

		if (ProfilerConfiguration.SERIALIZER.equals("kryo")) {
			conf = configureEfficientSerialization(conf);
		}

		sc = new JavaSparkContext(conf);
		hiveContext = new HiveContext(sc.sc());
		hiveContext.setConf("spark.sql.dialect", ProfilerConfiguration.SQL_DIALECT);


		/* Run query and get result */
		System.out.println("[PROFILER-INFO] Analyzing profile statistics for: [" + queryString + "]");
		resultDF = hiveContext.sql(queryString);


		/* Update schema map and broadcast it*/
		populateAndBroadcastSchemaMap(resultDF, sc);


		/* Get profile statistics and write to table */
		profileStatistics(resultDF).writeModel(sc, hiveContext);


		/* Wrap up */
		System.out.println("[PROFILER-INFO] Profiling finished.");
		sc.close();
	}



	/**
	 * Profile statistics for data frame
	 * @param resultDF data frame to analyze
	 * @return StatisticsModel
	 */
	public static StatisticsModel profileStatistics(DataFrame resultDF) {

		JavaPairRDD<Tuple2<Integer, Object>, Integer> columnValueCounts;
		StatisticsModel profileStatisticsModel;

		/* Get ((column index, column value), count) */
		columnValueCounts = resultDF
				.javaRDD()
				.flatMapToPair(new IndividualColumnValueCounts())
				.reduceByKey(new TotalColumnValueCounts());

		/* Generate the profile model */
		profileStatisticsModel = columnValueCounts
				.mapPartitions(new PartitionLevelModels(bSchemaMap))
				.reduce(new CombineModels());

		return profileStatisticsModel;
	}


	/**
	 * Check command line arguments
	 * @param args list of command line arguments
	 * @return query to run (null if invalid arguments)
	 */
	public static String checkCommandLineArgs(String[] args) {
	    System.out.println("Running Spark Profiler with the following command line " + args.length + " args (comma separated): " + StringUtils.join(args, ","));
		if (args.length<4) {
			System.out.println("Invalid number of command line arguments (" + args.length + ")");
			showCommandLineArgs();
			return null;
		}

		String retVal;

		String profileObjectType = args[0];
		String profileObjectDesc = args[1];
		Integer n = Integer.valueOf(args[2]);
		String profileOutputTable = args[3];

		String inputAndOutputTablePartitionKey = "ALL";

	    if (args.length >= 5) {
			inputAndOutputTablePartitionKey = args[4];
		}

		switch (profileObjectType) {
			case "table":
				retVal = "select * from " + HiveUtils.quoteIdentifier(profileObjectDesc);
				if (inputAndOutputTablePartitionKey != null && !"ALL".equalsIgnoreCase(inputAndOutputTablePartitionKey)) {
					retVal += " where " + HiveUtils.quoteIdentifier(ProfilerConfiguration.INPUT_TABLE_PARTITION_COLUMN_NAME) + " = " + HiveUtils.quoteString(inputAndOutputTablePartitionKey);
				}
				break;
			case "query":
				retVal = profileObjectDesc;
				break;
			default:
				System.out.println("Illegal command line argument for object type (" + profileObjectType + ")");
				showCommandLineArgs();
				return null;
		}


		if (n <= 0) {
			System.out.println("Illegal command line argument for n for top_n values (" + n + ")");
			showCommandLineArgs();
			return null;
		}
		else {
			ProfilerConfiguration.NUMBER_OF_TOP_N_VALUES = n;
		}


		if (!setOutputTableDBAndName(profileOutputTable)) {
			System.out.println("Illegal command line argument for output table (" + profileOutputTable + ")");
			showCommandLineArgs();
			return null;
		}

		ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY = inputAndOutputTablePartitionKey;

		return retVal;
	}


	/*
	 * Set output database and table
	 */
	private static boolean setOutputTableDBAndName(String profileOutputTable) {

		Boolean retVal = true;
		String[] tableNameParts = profileOutputTable.split("\\.");

		if (tableNameParts.length == 1) {
			//output db remains as 'default'
			ProfilerConfiguration.OUTPUT_TABLE_NAME = tableNameParts[0];
		}
		else if (tableNameParts.length == 2){
			ProfilerConfiguration.OUTPUT_DB_NAME = tableNameParts[0];
			ProfilerConfiguration.OUTPUT_TABLE_NAME = tableNameParts[1];
		}
		else {
			retVal = false;
		}

		return retVal;
	}


	/*
	 * Configure efficient serialization via kryo
	 */
	private static SparkConf configureEfficientSerialization(SparkConf conf) {

		List<Class<?>> serializeClassesList;
		Class<?>[] serializeClassesArray;

		conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

		serializeClassesList = new ArrayList<>();
		serializeClassesList.add(ColumnStatistics.class);
		serializeClassesList.add(BigDecimalColumnStatistics.class);
		serializeClassesList.add(BooleanColumnStatistics.class);
		serializeClassesList.add(ByteColumnStatistics.class);
		serializeClassesList.add(DateColumnStatistics.class);
		serializeClassesList.add(DoubleColumnStatistics.class);
		serializeClassesList.add(FloatColumnStatistics.class);
		serializeClassesList.add(IntegerColumnStatistics.class);
		serializeClassesList.add(LongColumnStatistics.class);
		serializeClassesList.add(ShortColumnStatistics.class);
		serializeClassesList.add(StringColumnStatistics.class);
		serializeClassesList.add(TimestampColumnStatistics.class);
		serializeClassesList.add(UnsupportedColumnStatistics.class);
		serializeClassesList.add(StatisticsModel.class);
		serializeClassesList.add(TopNDataItem.class);
		serializeClassesList.add(TopNDataList.class);
		serializeClassesList.add(OutputRow.class);
		serializeClassesList.add(OutputWriter.class);


		serializeClassesArray = new Class[serializeClassesList.size()];
		for (int i = 0; i < serializeClassesList.size(); i++) {
			serializeClassesArray[i] = serializeClassesList.get(i);
		}

		conf.registerKryoClasses(serializeClassesArray);
		return conf;
	}



	/*
	 *  Print column value counts
	 *  Only use for debugging, since output can be quite large in volume
	 */
	@SuppressWarnings("unused")
	private static void printColumnValueCounts(JavaPairRDD<Tuple2<Integer, Object>, Integer> columnValueCounts) {

		for (Tuple2<Tuple2<Integer, Object>, Integer> columnValueCount: columnValueCounts.collect()) {
			System.out.println(columnValueCount);
		}

	}


	/* Populate schema map */
	public static void populateAndBroadcastSchemaMap(DataFrame df, JavaSparkContext sc) {

		for (int i=0; i < df.schema().fields().length; i++) {
			SchemaInfo.schemaMap.put(i, df.schema().fields()[i]);
		}

		bSchemaMap = sc.broadcast(SchemaInfo.schemaMap);
	}


	/*
	 * Print schema of query result
	 * Only use for debugging, since this is already written out as result of profiling
	 */
	@SuppressWarnings("unused")
	private static void printSchema(StructField[] schemaFields) {

		System.out.println("=== Schema ===");
		System.out.println("[Field#\tName\tDataType\tNullable?\tMetadata]");
		for (int i = 0; i < schemaFields.length; i++) {
			String output = "Field #" + i + "\t"
					+ schemaFields[i].name() + "\t"
					+ schemaFields[i].dataType() + "\t"
					+ schemaFields[i].nullable() + "\t"
					+ schemaFields[i].metadata();

			System.out.println(output);
		}
	}



	/* Show required command-line arguments */
	private static void showCommandLineArgs()  {

		System.out.println("*** \nInfo: Required command line arguments:\n"
				+ "1. object type: valid values are {table, query}\n"
				+ "2. object description: valid values are {<database.table>, <query>}\n"
				+ "3. n for top_n values: valid value is {<integer>}\n"
				+ "4. output table: valid values are {<table>, <database.table>}"
				+ "\n"
				+ "Info: Optional command line argument:\n"
				+ "5. partition_key: valid value is {<string>}\n\n"
				+ "(Note: Only alphanumeric and underscore characters for table names and partition key)"
				+ "\n***");
	}
}
