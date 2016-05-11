package com.thinkbiganalytics.spark.util

import org.apache.hadoop.hive.common.`type`.HiveDecimal
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo
import org.apache.spark.sql.types.{MapType, StructType, _}

import java.util

/** Utility methods for `DataType`s. */
object DataTypeUtils {

    /** Converts the specified Spark SQL type to a Hive ObjectInspector.
      *
      * @param dataType the Spark SQL type
      * @return the Hive ObjectInspector
      */
    def toObjectInspector(dataType: DataType): ObjectInspector = dataType match {
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
        case ArrayType(elementType, _) => ObjectInspectorFactory.getStandardListObjectInspector(
            toObjectInspector(elementType))
        case MapType(keyType, valueType, _) => ObjectInspectorFactory.getStandardMapObjectInspector(
            toObjectInspector(keyType), toObjectInspector(valueType))
        case StructType(fields) =>
            val fieldNames = util.Arrays.asList(fields.map(_.name): _*)
            val fieldInspectors = util.Arrays.asList(fields.map(f => toObjectInspector(f.dataType)): _*)
            ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldInspectors)

        // Unsupported types
        case _ => throw new IllegalArgumentException
    }
}
