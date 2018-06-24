package com.thinkbiganalytics.spark.service

import java.util

import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, ObjectInspectorFactory}
import org.apache.spark.sql.RowFactory
import org.apache.spark.sql.types._
import collection.JavaConversions._
import org.junit.{Assert, Test}

import scala.collection.JavaConverters

class AbstractDataSetConverterServiceTest {

    private val converterService = new AbstractDataSetConverterService {
        override protected def findHiveObjectConverter(dataType: DataType): Option[ObjectInspectorConverters.Converter] = Option.empty

        override protected def findHiveObjectInspector(dataType: DataType): Option[ObjectInspector] = Option.empty
    }

    /** Verify converting Spark object to Hive object. */
    @Test
    def getHiveObjectConverter(): Unit = {
        // Test identity converter
        val identityConverter = converterService.getHiveObjectConverter(IntegerType)
        Assert.assertEquals(42, identityConverter.convert(42))


        //Test Map Conversion
        var m :  java.util.Map[String,String] = new util.HashMap[String,String]();
        m.put("name","a name");
        m.put("description","some desc")

        var scalaMap = m.toMap.asInstanceOf[Map[String, String]]
        var mt :org.apache.spark.sql.types.MapType = DataTypes.createMapType(DataTypes.StringType,DataTypes.StringType);
        var row = RowFactory.create(scalaMap);
        val converter = converterService.getHiveObjectConverter(mt)
        var convertedMap = converter.convert(row.get(0));
        Assert.assertEquals(2, convertedMap.asInstanceOf[java.util.Map[String,String]].size)




    }

    /** Verify converting Spark SQL types to Hive object inspectors. */
    @Test
    def toObjectInspector(): Unit = {
        // Test type conversions
        Assert.assertEquals(PrimitiveObjectInspectorFactory.javaIntObjectInspector, converterService.getHiveObjectInspector(IntegerType))
        Assert.assertEquals(ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector),
            converterService.getHiveObjectInspector(ArrayType(IntegerType)))
        Assert.assertEquals(ObjectInspectorFactory.getStandardMapObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector, PrimitiveObjectInspectorFactory.javaIntObjectInspector),
            converterService.getHiveObjectInspector(MapType(StringType, IntegerType)))

        // Test decimal type conversion
        val smallDecimalType = new DecimalType(10, 0)
        val smallDecimalInspector = converterService.getHiveObjectInspector(smallDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, smallDecimalInspector.precision())
        Assert.assertEquals(0, smallDecimalInspector.scale())

        val largeDecimalType = new DecimalType()
        val largeDecimalInspector = converterService.getHiveObjectInspector(largeDecimalType).asInstanceOf[JavaHiveDecimalObjectInspector]
        Assert.assertEquals(10, largeDecimalInspector.precision())
        Assert.assertEquals(0, largeDecimalInspector.scale())

        // Test struct type conversion
        val dataType = StructType(Array(StructField("id", IntegerType)))
        val structOI = ObjectInspectorFactory.getStandardStructObjectInspector(util.Arrays.asList("id"), util.Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector))
        Assert.assertEquals(structOI, converterService.getHiveObjectInspector(dataType))


    }
}
