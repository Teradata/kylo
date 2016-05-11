package com.thinkbiganalytics.spark.util

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.spark.sql.types.{PrecisionInfo, StructField, StructType, _}
import org.junit.{Assert, Test}

import java.util

class DataTypeUtilsTest {
    /** Verify converting Spark SQL types to Hive object inspectors. */
    @Test
    def toObjectInspector(): Unit = {
        // Test type conversions
        Assert.assertEquals(PrimitiveObjectInspectorFactory.javaIntObjectInspector, DataTypeUtils.toObjectInspector(IntegerType))
        Assert.assertEquals(
            ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector),
            DataTypeUtils.toObjectInspector(ArrayType(IntegerType)))
        Assert.assertEquals(ObjectInspectorFactory.getStandardMapObjectInspector(
            PrimitiveObjectInspectorFactory.javaStringObjectInspector,
            PrimitiveObjectInspectorFactory.javaIntObjectInspector),
            DataTypeUtils.toObjectInspector(MapType(StringType, IntegerType)))

        // Test decimal type conversion
        val smallDecimalType = new DecimalType(Option.apply(new PrecisionInfo(10, 0)))
        val smallDecimalInspector = DataTypeUtils.toObjectInspector(smallDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, smallDecimalInspector.precision())
        Assert.assertEquals(0, smallDecimalInspector.scale())

        val largeDecimalType = new DecimalType(Option.empty)
        val largeDecimalInspector = DataTypeUtils.toObjectInspector(largeDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, largeDecimalInspector.precision())
        Assert.assertEquals(0, largeDecimalInspector.scale())

        // Test struct type conversion
        val dataType = StructType(Array(StructField("id", IntegerType)))
        val structOI = ObjectInspectorFactory.getStandardStructObjectInspector(util.Arrays.asList(
            "id"), util.Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector))
        Assert.assertEquals(structOI, DataTypeUtils.toObjectInspector(dataType))
    }
}
