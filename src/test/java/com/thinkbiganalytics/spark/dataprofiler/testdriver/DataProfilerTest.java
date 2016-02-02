package com.thinkbiganalytics.spark.dataprofiler.testdriver;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.Profiler;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.DateColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.DoubleColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.FloatColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.IntegerColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.LongColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.ShortColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.StringColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.TimestampColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.BigDecimalColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.BooleanColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.ByteColumnTestSuite;
import com.thinkbiganalytics.spark.dataprofiler.testsuites.DataChecksTestSuite;

/**
 * Tests for data profiler
 * @author jagrut sharma
 *
 */

@RunWith(Suite.class)
@SuiteClasses({ 
			IntegerColumnTestSuite.class, 
			StringColumnTestSuite.class, 
			DoubleColumnTestSuite.class, 
			DateColumnTestSuite.class,
			BooleanColumnTestSuite.class,
			TimestampColumnTestSuite.class,
			LongColumnTestSuite.class,
			FloatColumnTestSuite.class,
			ShortColumnTestSuite.class,
			ByteColumnTestSuite.class,
			BigDecimalColumnTestSuite.class,
			DataChecksTestSuite.class
		})

public class DataProfilerTest {
	
	static String SPARK_MASTER = "local[*]";
	static String APP_NAME = "Profiler Test";
	public static String EMPTY_STRING = "";
	public static double epsilon = 0.0001d;
	public static double epsilon2 = 3000.0d; //only used for long-variance, since they are extremely large numbers
	
	static int i;
	static StructField[] schemaFields;
	static List<Row> rows;
	static SparkConf conf;
	static JavaSparkContext sc;
	static JavaRDD<Row> dataRDD;
	static DataFrame dataDF;
	static SQLContext sqlContext;
	static StructType schema;
	static StatisticsModel statsModel;
	public static Map<Integer, ColumnStatistics> columnStatsMap;
	
    
	
	@BeforeClass 
	public static void setUpClass() {
		System.out.println("||=== Starting run for DataProfilerTest ===||");
		
		//TODO: Add more fields. Remember to update the array size.
		schemaFields = new StructField[14];
		schemaFields[0] = DataTypes.createStructField("id", DataTypes.IntegerType, true);
		schemaFields[1] = DataTypes.createStructField("firstname", DataTypes.StringType, true);
		schemaFields[2] = DataTypes.createStructField("lastname", DataTypes.StringType, true);
		schemaFields[3] = DataTypes.createStructField("age", DataTypes.IntegerType, true);
		schemaFields[4] = DataTypes.createStructField("description", DataTypes.StringType, true);
		schemaFields[5] = DataTypes.createStructField("height", DataTypes.DoubleType, true);
		schemaFields[6] = DataTypes.createStructField("joindate", DataTypes.DateType, true);
		schemaFields[7] = DataTypes.createStructField("lifemember", DataTypes.BooleanType, true);
		schemaFields[8] = DataTypes.createStructField("lastlogin", DataTypes.TimestampType, true);
		schemaFields[9] = DataTypes.createStructField("phash", DataTypes.LongType, true);
		schemaFields[10] = DataTypes.createStructField("weight", DataTypes.FloatType, true);
		schemaFields[11] = DataTypes.createStructField("credits", DataTypes.ShortType, true);
		schemaFields[12] = DataTypes.createStructField("ccode", DataTypes.ByteType, true);
		schemaFields[13] = DataTypes.createStructField("score", DataTypes.createDecimalType(7, 5), true);
		
		schema = DataTypes.createStructType(schemaFields);

		//TODO: Add additional columns corresponding to the fields you add above
		rows = new ArrayList<Row>();
		
		rows.add(RowFactory.create(
				new Integer(1), 
				new String("Jon"), 
				new String("Wright"), 
				new Integer(14), 
				new String("Jon::Wright"), 
				new Double(5.85d), 
				Date.valueOf("2010-05-04"),
				new Boolean(true),
				Timestamp.valueOf("2008-05-06 23:10:10"),
				new Long(1456890911l),
				new Float(40.2f),
				new Short((short)100),
				new Byte((byte)99),
				new BigDecimal(String.valueOf(1.567))));
		
		rows.add(RowFactory.create(
				new Integer(2), 
				new String("Jon"), 
				new String("Hudson"), 
				null, 
				new String("Jon::Hudson"), 
				new Double(5.85d), 
				Date.valueOf("1990-10-25"), 
				null,
				Timestamp.valueOf("2011-01-08 11:25:45"),
				new Long(7638962135l),
				new Float(110.5f),
				new Short((short)100),
				new Byte((byte)99),
				new BigDecimal(String.valueOf(8.223))));
		
		rows.add(RowFactory.create(
				new Integer(3), 
				new String("Rachael"), 
				new String("Hu"), 
				new Integer(40), 
				new String("Rachael::Hu"), 
				new Double(6.22d), 
				Date.valueOf("1990-10-25"), 
				new Boolean(true),
				Timestamp.valueOf("2011-01-08 11:25:45"),
				new Long(2988626110l),
				new Float(160.7f),
				new Short((short)1400),
				new Byte((byte)99),
				new BigDecimal(String.valueOf(1.567))));
		
		rows.add(RowFactory.create(
				new Integer(4), 
				EMPTY_STRING, 
				EMPTY_STRING, 
				new Integer(40), 
				null, 
				null, 
				Date.valueOf("1956-11-12"), 
				new Boolean(true),
				Timestamp.valueOf("2008-05-06 23:10:10"),
				new Long(2988626110l),
				null,
				null,
				new Byte((byte)99),
				null));
		
		rows.add(RowFactory.create(
				new Integer(5), 
				new String("Rachael"), 
				EMPTY_STRING, 
				new Integer(22), 
				new String("Rachael::"), 
				new Double(5.85d), 
				Date.valueOf("2005-12-24"),
				new Boolean(false),
				Timestamp.valueOf("2008-05-06 23:10:10"),
				new Long(8260467621l),
				new Float(160.7f),
				new Short((short)100),
				null,
				new BigDecimal(String.valueOf(4.343))));
		
		rows.add(RowFactory.create(
				new Integer(6), 
				new String("Elizabeth"), 
				new String("Taylor"), 
				new Integer(40), 
				new String("Elizabeth::Taylor"), 
				new Double(5.85d), 
				Date.valueOf("2011-08-08"),
				null,
				Timestamp.valueOf("2016-01-14 14:20:20"),
				new Long(8732866249l),
				null,
				new Short((short)1400),
				null,
				new BigDecimal(String.valueOf(4.343))));
		
		rows.add(RowFactory.create(
				new Integer(7), 
				new String("Jon"), 
				new String("Taylor"), 
				new Integer(18), 
				new String("Jon::Taylor"), 
				null, 
				Date.valueOf("2011-08-08"),
				new Boolean(true),
				Timestamp.valueOf("2011-01-08 11:25:45"),
				new Long(2988626110l),
				new Float(110.5f),
				new Short((short)500),
				new Byte((byte)40),
				new BigDecimal(String.valueOf(4.343))));
		
		rows.add(RowFactory.create(
				new Integer(8), 
				new String("Rachael"), 
				EMPTY_STRING, 
				new Integer(22), 
				new String("Rachael::"),
				new Double(4.37d), 
				Date.valueOf("2011-08-08"),
				new Boolean(false),
				Timestamp.valueOf("2008-05-06 23:10:10"),
				new Long(8782348100l),
				null,
				null,
				null,
				null));
		
		rows.add(RowFactory.create(
				new Integer(9), 
				EMPTY_STRING, 
				new String("Edmundson Jr"), 
				new Integer(11), 
				new String("::Edmundson Jr"), 
				new Double(4.88d), 
				Date.valueOf("2007-06-07"),
				new Boolean(false),
				Timestamp.valueOf("2007-03-16 08:24:37"),
				null,
				new Float(155.3f),
				new Short((short)0),
				new Byte((byte)99),
				new BigDecimal(String.valueOf(1.567))));
		
		rows.add(RowFactory.create(
				new Integer(10), 
				new String("Jon"), 
				EMPTY_STRING, 
				new Integer(65), 
				new String("Jon::"), 
				null, 
				Date.valueOf("1975-04-04"),
				new Boolean(true),
				Timestamp.valueOf("2007-03-16 08:24:31"),
				null,
				new Float(180.6f),
				new Short((short)5000),
				new Byte((byte)2),
				new BigDecimal(String.valueOf(4.343))));
		
		
		conf = new SparkConf().setMaster(SPARK_MASTER).setAppName(APP_NAME);
		sc = new JavaSparkContext(conf);
		sqlContext = new SQLContext(sc);
		
		dataRDD = sc.parallelize(rows);
		dataDF = sqlContext.createDataFrame(dataRDD, schema);
		
		/* Enable to debug contents of test data */
		/*
		for (Row r: dataRDD.collect()) {
			System.out.println(r.toString());
		}
		*/
		
		statsModel = Profiler.profileStatistics(dataDF);
		columnStatsMap = statsModel.getColumnStatisticsMap();		
	}
	
	

    @AfterClass 
    public static void tearDownClass() { 
        sc.close();
		System.out.println("||=== Completed run for DataProfilerTest ===||");
    }

}