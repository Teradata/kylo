package com.thinkbiganalytics.spark.util

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.spark.mllib.linalg.{VectorUDT, Vectors}
import org.apache.spark.sql.types._
import org.junit.{Assert, Test}

import java.util

class DataTypeUtilsTest {

    /** Verify converting Spark object to Hive object. */
    @Test
    def getHiveObjectConverter(): Unit = {
        // Test Vector converter
        val vectorConverter = DataTypeUtils.getHiveObjectConverter(new VectorUDT)
        Assert.assertArrayEquals(Array(2.0, 4.0, 6.0), vectorConverter.convert(Vectors.dense(2.0, 4.0, 6.0)).asInstanceOf[Array[Double]], 0.1)

        // Test identity converter
        val identityConverter = DataTypeUtils.getHiveObjectConverter(IntegerType)
        Assert.assertEquals(42, identityConverter.convert(42))
    }

    /** Verify converting Spark SQL types to Hive object inspectors. */
    @Test
    def toObjectInspector(): Unit = {
        // Test type conversions
        Assert.assertEquals(PrimitiveObjectInspectorFactory.javaIntObjectInspector, DataTypeUtils.getHiveObjectInspector(IntegerType))
        Assert.assertEquals(ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector), DataTypeUtils.getHiveObjectInspector(ArrayType(IntegerType)))
        Assert.assertEquals(ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaIntObjectInspector),
            DataTypeUtils.getHiveObjectInspector(MapType(StringType, IntegerType)))

        // Test decimal type conversion
        val smallDecimalType = new DecimalType(10, 0)
        val smallDecimalInspector = DataTypeUtils.getHiveObjectInspector(smallDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, smallDecimalInspector.precision())
        Assert.assertEquals(0, smallDecimalInspector.scale())

        val largeDecimalType = new DecimalType()
        val largeDecimalInspector = DataTypeUtils.getHiveObjectInspector(largeDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, largeDecimalInspector.precision())
        Assert.assertEquals(0, largeDecimalInspector.scale())

        // Test struct type conversion
        val dataType = StructType(Array(StructField("id", IntegerType)))
        val structOI = ObjectInspectorFactory.getStandardStructObjectInspector(util.Arrays.asList("id"), util.Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector))
        Assert.assertEquals(structOI, DataTypeUtils.getHiveObjectInspector(dataType))
    }
}
