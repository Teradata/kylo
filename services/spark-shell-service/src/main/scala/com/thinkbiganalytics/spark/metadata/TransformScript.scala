package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.db.model.query.{QueryResult, QueryResultColumn}
import com.thinkbiganalytics.spark.util.{DataTypeUtils, HiveUtils}

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.slf4j.LoggerFactory

import java.util
import java.util.concurrent.Callable
import java.util.regex.Pattern

import scala.collection.JavaConversions

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript(destination: String, sendResults: Boolean, sqlContext: SQLContext) {

    val log = LoggerFactory.getLogger(classOf[TransformScript])

    /** Prefix for display names that are different from the field name */
    val DISPLAY_NAME_PREFIX = "col"

    /** Pattern for field names */
    val FIELD_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$")

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): Any = {
        if (sendResults) new QueryResultCallable else new InsertHiveCallable
    }

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected def dataFrame: DataFrame

    /** Gets the columns for the specified schema.
      *
      * @param schema the DataFrame schema
      * @return the columns
      */
    protected[metadata] def getColumns(schema: StructType): Array[QueryResultColumn] = {
        val columns = Array.newBuilder[QueryResultColumn]
        var index = 1

        for (field <- schema.fields) {
            val column = new QueryResultColumn
            column.setDataType(DataTypeUtils.toObjectInspector(field.dataType).getTypeName)
            column.setHiveColumnLabel(field.name)
            column.setTableName(destination)

            if (FIELD_PATTERN.matcher(field.name).matches()) {
                // Use original name if alphanumeric
                column.setDisplayName(field.name)
                column.setField(field.name)
            } else {
                // Generate name for non-alphanumeric fields
                var name: String = null
                do {
                    name = DISPLAY_NAME_PREFIX + index
                    index += 1

                    try {
                        schema(name)
                        name = null
                    } catch {
                        case e: IllegalArgumentException =>
                    }
                } while (name == null)

                column.setDisplayName(name)
                column.setField(name)
            }

            columns += column
        }

        columns.result()
    }

    /** Fetches or re-generates the results of the parent transformation, if available.
      *
      * @return the parent results
      */
    protected def parent: DataFrame = {
        try {
            sqlContext.read.table(parentTable)
        }
        catch {
            case e: Exception =>
                log.trace("Exception reading parent table: {}", e.toString)
                log.debug("Parent table not found: {}", parentTable)
                parentDataFrame
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

    /** Converts the specified DataFrame schema to a CREATE TABLE statement.
      *
      * @param schema the DataFrame schema
      * @return the CREATE TABLE statement
      */
    protected[metadata] def toSQL(schema: StructType): String = {
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
            sql.append(DataTypeUtils.toObjectInspector(field.dataType).getTypeName)
        }

        sql.append(") STORED AS ORC")
        sql.toString()
    }

    /** Writes the `DataFrame` results to a Hive table. */
    private class InsertHiveCallable extends Callable[Unit] {
        override def call(): Unit = {
            val df = dataFrame
            sqlContext.sql(toSQL(df.schema))
            df.write.mode(SaveMode.Overwrite).insertInto(destination)
        }
    }

    /** Stores the `DataFrame` results in a [[com.thinkbiganalytics.db.model.query.QueryResult]] and returns the object. */
    private class QueryResultCallable extends Callable[QueryResult] {
        override def call(): QueryResult = {
            // Cache data frame
            val cache = dataFrame.cache
            cache.registerTempTable(destination)

            // Build result object
            val result = new QueryResult("SELECT * FROM " + destination)

            val columns = getColumns(cache.schema)
            result.setColumns(JavaConversions.seqAsJavaList(columns))

            for (row <- cache.collect()) {
                val r = new util.HashMap[String, Object]()
                for (i <- columns.indices) {
                    r.put(columns(i).getDisplayName, row.getAs(i))
                }
                result.addRow(r)
            }

            result
        }
    }
}
