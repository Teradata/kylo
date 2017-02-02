package com.thinkbiganalytics.spark.util

import org.apache.hadoop.hive.common.`type`.HiveDecimal
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, ObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo
import org.apache.spark.mllib.linalg.{Vector, VectorUDT}
import org.apache.spark.sql.types._

import scala.collection.{JavaConversions, mutable}

/** Utility methods for `DataType`s. */
object DataTypeUtils {

    /** Gets a converter for transforming objects from the specified Spark SQL type to a Hive type.
      *
      * @param dataType the Spark SQL type
      * @return the converter to a Hive object
      */
    def getHiveObjectConverter(dataType: DataType): ObjectInspectorConverters.Converter = dataType match {
        case ArrayType(_, _) => new Converter {
            override def convert(o: Object): Object = o.asInstanceOf[mutable.WrappedArray[Object]].toArray
        }
        case vectorType: VectorUDT => new Converter {
            override def convert(o: Object): Object = o.asInstanceOf[Vector].toArray
        }
        case _ => new ObjectInspectorConverters.IdentityConverter
    }

    /** Converts the specified Spark SQL type to a Hive ObjectInspector.
      *
      * @param dataType the Spark SQL type
      * @return the Hive ObjectInspector
      */
    def getHiveObjectInspector(dataType: DataType): ObjectInspector = dataType match {
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

        // User-defined type
        case vectorType: VectorUDT => ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector)

        // Unsupported types
        case _ => throw new IllegalArgumentException("Unsupported data type: " + dataType)
    }
}
