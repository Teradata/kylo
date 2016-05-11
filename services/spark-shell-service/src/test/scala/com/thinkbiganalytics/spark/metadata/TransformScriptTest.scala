package com.thinkbiganalytics.spark.metadata

import org.apache.hadoop.hive.common.`type`.HiveDecimal
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.junit.{Assert, Test}
import org.mockito.Mockito

import java.util

class TransformScriptTest {
    /** Verify getting columns for a schema. */
    @Test
    def getColumns(): Unit = {
        // Mock transform script
        val script = new TransformScript("mydest", true, Mockito.mock(classOf[SQLContext])) {
            override protected def dataFrame: DataFrame = null
        }

        // Test columns
        val struct = StructType(
            StructField("id", LongType) ::
            StructField("SUM(amount)", DoubleType) ::
            StructField("AVG(amount)", DoubleType) ::
            StructField("col2", StringType) ::
            Nil
        )

        val columns = script.getColumns(struct)
        Assert.assertEquals(4, columns.length)

        Assert.assertEquals("bigint", columns(0).getDataType)
        Assert.assertEquals("id", columns(0).getDisplayName)
        Assert.assertEquals("id", columns(0).getField)
        Assert.assertEquals("id", columns(0).getHiveColumnLabel)

        Assert.assertEquals("double", columns(1).getDataType)
        Assert.assertEquals("col1", columns(1).getDisplayName)
        Assert.assertEquals("col1", columns(1).getField)
        Assert.assertEquals("SUM(amount)", columns(1).getHiveColumnLabel)

        Assert.assertEquals("double", columns(2).getDataType)
        Assert.assertEquals("col3", columns(2).getDisplayName)
        Assert.assertEquals("col3", columns(2).getField)
        Assert.assertEquals("AVG(amount)", columns(2).getHiveColumnLabel)

        Assert.assertEquals("string", columns(3).getDataType)
        Assert.assertEquals("col2", columns(3).getDisplayName)
        Assert.assertEquals("col2", columns(3).getField)
        Assert.assertEquals("col2", columns(3).getHiveColumnLabel)
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
