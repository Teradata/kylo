package com.thinkbiganalytics.spark.service

import java.util

import org.apache.hadoop.hive.common.`type`.HiveDecimal
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, ObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo
import org.apache.spark.sql.types._

import scala.collection.{JavaConversions, mutable}

abstract class AbstractDataSetConverterService extends DataSetConverterService {

    override def getHiveObjectConverter(dataType: DataType): ObjectInspectorConverters.Converter = dataType match {
        case ArrayType(_, _) => new ObjectInspectorConverters.Converter {
            override def convert(o: Object): Object = o.asInstanceOf[mutable.WrappedArray[Object]].toArray
        }
        case MapType(keyType, valueType, _) => {
            val keyConverter = getHiveObjectConverter(keyType)
            val valueConverter = getHiveObjectConverter(valueType)
            new ObjectInspectorConverters.Converter {
                override def convert(o: scala.Any): AnyRef = {
                    val scalaMap = o.asInstanceOf[Map[Object, Object]]
                    val javaMap = new util.HashMap[Object, Object](scalaMap.size)
                    scalaMap.foreach(entry => javaMap.put(keyConverter.convert(entry._1), valueConverter.convert(entry._2)))
                    javaMap
                }
            }
        }
        case _ => findHiveObjectConverter(dataType).getOrElse(new ObjectInspectorConverters.IdentityConverter)
    }

    override def getHiveObjectInspector(dataType: DataType): ObjectInspector = dataType match {
        // Primitive types
        case BinaryType => PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector
        case BooleanType => PrimitiveObjectInspectorFactory.javaBooleanObjectInspector
        case ByteType => PrimitiveObjectInspectorFactory.javaByteObjectInspector
        case DateType => PrimitiveObjectInspectorFactory.javaDateObjectInspector
        case decimalType: DecimalType =>
            val precision = if (decimalType.precision >= 0) decimalType.precision else HiveDecimal.MAX_PRECISION
            val scale = if (decimalType.scale >= 0) decimalType.scale else HiveDecimal.MAX_SCALE
            new JavaHiveDecimalObjectInspector(new DecimalTypeInfo(precision, scale))
        case DoubleType => PrimitiveObjectInspectorFactory.javaDoubleObjectInspector
        case FloatType => PrimitiveObjectInspectorFactory.javaFloatObjectInspector
        case IntegerType => PrimitiveObjectInspectorFactory.javaIntObjectInspector
        case LongType => PrimitiveObjectInspectorFactory.javaLongObjectInspector
        case NullType => PrimitiveObjectInspectorFactory.javaVoidObjectInspector
        case ShortType => PrimitiveObjectInspectorFactory.javaShortObjectInspector
        case StringType => PrimitiveObjectInspectorFactory.javaStringObjectInspector
        case TimestampType => PrimitiveObjectInspectorFactory.javaTimestampObjectInspector

        // Complex types
        case ArrayType(elementType, _) => ObjectInspectorFactory.getStandardListObjectInspector(getHiveObjectInspector(elementType))
        case MapType(keyType, valueType, _) => ObjectInspectorFactory.getStandardMapObjectInspector(getHiveObjectInspector(keyType), getHiveObjectInspector(valueType))
        case StructType(fields) =>
            val fieldNames = JavaConversions.seqAsJavaList(fields.map(_.name))
            val fieldInspectors = JavaConversions.seqAsJavaList(fields.map(f => getHiveObjectInspector(f.dataType)))
            ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldInspectors)

        // User-defined types
        case _ => findHiveObjectInspector(dataType).getOrElse(throw new IllegalArgumentException("Unsupported data type: " + dataType))
    }

    /** Finds a converter for the specified Spark SQL type.
      *
      * @param dataType the Spark SQL type
      * @return the converter to a Hive object
      */
    protected def findHiveObjectConverter(dataType: DataType): Option[ObjectInspectorConverters.Converter]

    /** Finds a Hive ObjectInspector for the specified Spark SQL type.
      *
      * @param dataType the Spark SQL type
      * @return the Hive ObjectInspector
      */
    protected def findHiveObjectInspector(dataType: DataType): Option[ObjectInspector]
}
