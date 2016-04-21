package com.thinkbiganalytics.spark.metadata

import java.util

import com.thinkbiganalytics.db.model.query.{QueryResult, QueryResultColumn}
import com.thinkbiganalytics.spark.util.HiveUtils
import org.apache.hadoop.hive.serde2.objectinspector.primitive.{JavaHiveDecimalObjectInspector, PrimitiveObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorFactory}
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript(destination: String, sendResults: Boolean, sqlContext: SQLContext) {
    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): Any = {
        val df: DataFrame = dataFrame

        if (sendResults) {
            df.registerTempTable(destination)

            val result = new QueryResult("SELECT * FROM " + destination)
            val columns = new util.ArrayList[QueryResultColumn]
            val displayNameMap = new util.HashMap[String, Int]()
            for (field <- df.schema.fields) {
                val column = new QueryResultColumn
                column.setField(field.name)
                column.setHiveColumnLabel(field.name)
                column.setDisplayName(field.name)
                column.setTableName(destination)
                column.setDataType(field.dataType.toString)
                columns.add(column)
            }
            result.setColumns(columns)
            for (row <- df.collect()) {
                val r = new util.LinkedHashMap[String, Object]()
                for (i <- 0 until columns.size()) {
                    r.put(columns.get(i).getDisplayName, row.getAs(i))
                }
                result.addRow(r)
            }
            result
        }
        else {
            sqlContext.sql(toSQL(df.schema))
            df.write.mode(SaveMode.Overwrite).insertInto(destination)
        }
    }

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected def dataFrame: DataFrame

    /** Fetches or re-generates the results of the parent transformation, if available.
      *
      * @return the parent results
      */
    protected def parent: DataFrame = {
        try {
            sqlContext.read.table(parentTable)
        }
        catch {
            case e: Exception => parentDataFrame
        }
    }

    /** Re-generates the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentDataFrame: DataFrame = {
        throw new UnsupportedOperationException
    }

    /** Gets the name of the Hive table with the results of the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentTable: String = {
        throw new UnsupportedOperationException
    }

    /** Converts the specified Spark SQL type to a Hive ObjectInspector.
      *
      * @param dataType the Spark SQL type
      * @return the Hive ObjectInspector
      */
    def toInspector(dataType: DataType): ObjectInspector = dataType match {
        // Primitive types
        case BinaryType => PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector
        case BooleanType => PrimitiveObjectInspectorFactory.javaBooleanObjectInspector
        case ByteType => PrimitiveObjectInspectorFactory.javaByteObjectInspector
        case DateType => PrimitiveObjectInspectorFactory.javaDateObjectInspector
        case decimalType: DecimalType => new JavaHiveDecimalObjectInspector(new DecimalTypeInfo(decimalType.precision, decimalType
                .scale))
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
            toInspector(elementType))
        case MapType(keyType, valueType, _) => ObjectInspectorFactory.getStandardMapObjectInspector(
            toInspector(keyType), toInspector(valueType))
        case StructType(fields) =>
            val fieldNames = util.Arrays.asList(fields.map(_.name): _*)
            val fieldInspectors = util.Arrays.asList(fields.map(f => toInspector(f.dataType)): _*)
            ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldInspectors)

        // Unsupported types
        case _ => throw new IllegalArgumentException
    }

    /** Converts the specified DataFrame schema to a CREATE TABLE statement.
      *
      * @param schema the DataFrame schema
      * @return the CREATE TABLE statement
      */
    def toSQL(schema: StructType): String = {
        var first = true
        val sql = new StringBuilder

        sql.append("CREATE TABLE ")
        sql.append(HiveUtils.quoteIdentifier(destination))
        sql.append('(')

        for (field <- schema.fields) {
            if (first) first = false
            else sql.append(',')
            sql.append(HiveUtils.quoteIdentifier(field.name))
            sql.append(' ')
            sql.append(toInspector(field.dataType).getTypeName)
        }

        sql.append(") STORED AS ORC")
        sql.toString()
    }
}
