package com.thinkbiganalytics.spark.metadata

import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorFactory}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

abstract class TransformScript (destination: String, sqlContext: SQLContext) extends Runnable
{
    override def run (): Unit = {
        val df: DataFrame = dataFrame
        var schema = "CREATE TABLE " + destination + " ("
        var first = true

        for (field <- df.schema.fields) {
            if (first) first = false
            else schema = schema + ", "
            val name = field.name.replace('(', '_').replace(')', '_')
            schema = schema + name + " " + toInspector(field.dataType).getTypeName
        }

        schema = schema + ") STORED AS ORC"

        sqlContext.sql(schema)
        dataFrame.write.mode(SaveMode.Overwrite).insertInto(destination)
    }

    protected def dataFrame: DataFrame

    protected def parent: DataFrame = {
        try {
            sqlContext.read.table(parentTable)
        }
        catch {
            case e: Exception => parentDataFrame
        }
    }

    protected def parentDataFrame: DataFrame = {
        throw new UnsupportedOperationException
    }

    protected def parentTable: String = {
        throw new UnsupportedOperationException
    }

    // TODO
    def toInspector(dataType: DataType): ObjectInspector = dataType match {
        case ArrayType(tpe, _) =>
            ObjectInspectorFactory.getStandardListObjectInspector(toInspector(tpe))
        case MapType(keyType, valueType, _) =>
            ObjectInspectorFactory.getStandardMapObjectInspector(
                toInspector(keyType), toInspector(valueType))
        case StringType => PrimitiveObjectInspectorFactory.javaStringObjectInspector
        case IntegerType => PrimitiveObjectInspectorFactory.javaIntObjectInspector
        case DoubleType => PrimitiveObjectInspectorFactory.javaDoubleObjectInspector
        case BooleanType => PrimitiveObjectInspectorFactory.javaBooleanObjectInspector
        case LongType => PrimitiveObjectInspectorFactory.javaLongObjectInspector
        case FloatType => PrimitiveObjectInspectorFactory.javaFloatObjectInspector
        case ShortType => PrimitiveObjectInspectorFactory.javaShortObjectInspector
        case ByteType => PrimitiveObjectInspectorFactory.javaByteObjectInspector
        case NullType => PrimitiveObjectInspectorFactory.javaVoidObjectInspector
        case BinaryType => PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector
        case DateType => PrimitiveObjectInspectorFactory.javaDateObjectInspector
        case TimestampType => PrimitiveObjectInspectorFactory.javaTimestampObjectInspector
        // TODO decimal precision?
        case DecimalType() => PrimitiveObjectInspectorFactory.javaHiveDecimalObjectInspector
        case StructType(fields) =>
            ObjectInspectorFactory.getStandardStructObjectInspector(
                java.util.Arrays.asList(fields.map(f => f.name) : _*),
                java.util.Arrays.asList(fields.map(f => toInspector(f.dataType)) : _*))
    }
}
