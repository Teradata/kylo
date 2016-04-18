package com.thinkbiganalytics.spark.metadata

import java.util

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.typeinfo.{DecimalTypeInfo, HiveDecimalUtils}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.junit.{Assert, Test}
import org.mockito.Mockito

class TransformScriptTest {
    /** Verify converting Spark SQL types to Hive object inspectors. */
    @Test
    def toInspector(): Unit = {
        // Mock transform script
        val script = new TransformScript("mydest", false, Mockito.mock(classOf[SQLContext])) {
            override protected def dataFrame: DataFrame = null
        }

        // Test type conversions
        Assert.assertEquals(PrimitiveObjectInspectorFactory.javaIntObjectInspector, script.toInspector(IntegerType))
        Assert.assertEquals(ObjectInspectorFactory.getStandardListObjectInspector(
                PrimitiveObjectInspectorFactory.javaIntObjectInspector),
                script.toInspector(ArrayType(IntegerType)))
        Assert.assertEquals(ObjectInspectorFactory.getStandardMapObjectInspector(
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaIntObjectInspector), script.toInspector(MapType(StringType, IntegerType)))

        // Test decimal type conversion
        val decimalType = new DecimalType(Option.apply(new PrecisionInfo(10, 0)))
        val decimalInspector = script.toInspector(decimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, decimalInspector.precision())
        Assert.assertEquals(0, decimalInspector.scale())

        // Test struct type conversion
        val dataType = StructType(Array(StructField("id", IntegerType)))
        val structOI = ObjectInspectorFactory.getStandardStructObjectInspector(util.Arrays.asList(
            "id"), util.Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector))
        Assert.assertEquals(structOI, script.toInspector(dataType))
    }

    /** Verify converting a DataFrame schema to a CREATE TABLE statement. */
    @Test
    def toSQL(): Unit = {
        // Mock transform script
        val script = new TransformScript("mydest", false, Mockito.mock(classOf[SQLContext])) {
            override protected def dataFrame: DataFrame = null
        }

        // Test statement
        val schema = StructType(Array(StructField("event", StringType), StructField("ts",
            LongType), StructField("test`s", StringType)))
        Assert.assertEquals("CREATE TABLE `mydest`(`event` string,`ts` bigint,`test``s` string)" +
                " STORED AS ORC", script.toSQL(schema))
    }
}
