package com.thinkbiganalytics.spark.dataprofiler.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

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

import scala.Tuple2;

/**
 * Generate data profile statistics for a table/query, and write result to a table
 * @author jagrut sharma
 * @version 0.1
 *
 */
public class Profiler {
	
	@SuppressWarnings("unused")
	/**
	 * Main entry point into program
	 * @param list of args
	 */
	public static void main(String[] args) {

		/* Variables */
		SparkConf conf;
		JavaSparkContext sc;
		HiveContext hiveContext;
		DataFrame resultDF;
		String queryString;
		StructType resultSchema;
		StructField[] resultSchemaFields;
		String outputTableName;
		StatisticsModel statisticsModel;
		String partitionKey;

		
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

		
		/* Get profile statistics and write to table */
		profileStatistics(resultDF).writeModel(sc, hiveContext, ProfilerConfiguration.OUTPUT_TABLE_NAME, ProfilerConfiguration.PARTITION_KEY);
		
		
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
		
		/* Update schema map */
		populateSchemaMap(resultDF);

		/* Get ((column index, column value), count) */
		columnValueCounts = resultDF
				.javaRDD()
				.flatMapToPair(new IndividualColumnValueCounts())
				.reduceByKey(new TotalColumnValueCounts());
				
		/* Generate the profile model */
		profileStatisticsModel = columnValueCounts
				.mapPartitions(new PartitionLevelModels())
				.reduce(new CombineModels());

		return profileStatisticsModel;	
	}

	
	/**
	 * Check command line arguments
	 * @param args list of command line arguments
	 * @return query to run (null if invalid arguments)
	 */
	public static String checkCommandLineArgs(String[] args) {

		if (args.length<4) {
			System.out.println("Invalid number of command line arguments (" + args.length + ")");
			showCommandLineArgs();
			return null;
		}
		
		String retVal = null;
		
		String profileObjectType = args[0];	
		String profileObjectDesc = args[1];	
		Integer n = Integer.valueOf(args[2]);
		String profileOutputTable = args[3];
		String partitionKey = "ALL";
		if (args.length == 5) {
			partitionKey = args[4];
		}

		if (profileObjectType.equals("table")) {
			retVal = "select * from " + profileObjectDesc;
			if (partitionKey != null && !"ALL".equalsIgnoreCase(partitionKey)) {
				retVal += " where processing_dttm = '"+partitionKey+"'";
			}
		}
		else if (profileObjectType.equals("query")) {
			retVal = profileObjectDesc;
		}


		else {
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
		
		ProfilerConfiguration.OUTPUT_TABLE_NAME = profileOutputTable;
		ProfilerConfiguration.PARTITION_KEY = partitionKey;

		return retVal;
	}
	


	/*
	 * Configure efficient serialization via kryo
	 */
	private static SparkConf configureEfficientSerialization(SparkConf conf) {
		
		List<Class<?>> serializeClassesList;
		Class<?>[] serializeClassesArray;

		conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer"); 

		serializeClassesList = new ArrayList<Class<?>>();
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
	private static void populateSchemaMap(DataFrame df) {
		
		for (int i=0; i < df.schema().fields().length; i++) {
			SchemaInfo.schemaMap.put(i, df.schema().fields()[i]);
		}
	}


	/* 
	 * Print schema of query result
	 * Only use for debugging, since this is already written out as result of profiling 
	 */
	@SuppressWarnings("unused")
	private static void printSchema(StructField[] schemaFields) {
		
		System.out.println("=== Schema ===");
		System.out.println("[Field#\tName\tDataType\tNullable?\tMetadata]");;
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
				+ "2. object description: valid values are {<table name>, <query>}\n"
				+ "3. n for top_n values: valid value is {<integer>}\n"
				+ "4. output table: valid value is {<string>}"
				+ "\n***");
	}
}
