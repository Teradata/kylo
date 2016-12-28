package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.concurrent.Callable
import java.util.regex.Pattern

import com.thinkbiganalytics.discovery.model.{DefaultQueryResult, DefaultQueryResultColumn}
import com.thinkbiganalytics.hive.util.HiveUtils
import com.thinkbiganalytics.spark.rest.model.TransformResponse
import com.thinkbiganalytics.spark.util.DataTypeUtils
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  * @param sqlContext  the Spark SQL context
  */
abstract class TransformScript(destination: String, sendResults: Boolean, sqlContext: SQLContext) {

    val log = LoggerFactory.getLogger(classOf[TransformScript])

    /** Evaluates this transform script and stores the result in a Hive table. */
    def run(): Any = {
        if (sendResults) new QueryResultCallable else new InsertHiveCallable
    }

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected[metadata] def dataFrame: DataFrame

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

    /** Writes the `DataFrame` results to a Hive table.
      *
      * @deprecated Replaced with `QueryResultCallable`
      */
    @Deprecated
    private[metadata] class InsertHiveCallable extends Callable[Unit] {
        override def call(): Unit = {
            val df = dataFrame
            sqlContext.sql(toSQL(df.schema))
            df.write.mode(SaveMode.Overwrite).insertInto(destination)
        }

        /** Converts the specified DataFrame schema to a CREATE TABLE statement.
          *
          * @param schema the DataFrame schema
          * @return the CREATE TABLE statement
          */
        private[metadata] def toSQL(schema: StructType): String = {
            var first = true
            val sql = new StringBuilder

            sql.append("CREATE TABLE ")
            sql.append(HiveUtils.quoteIdentifier(destination))
            sql.append('(')

            for (field <- schema.fields) {
                if (first) first = false
                else sql.append(", ")
                sql.append(HiveUtils.quoteIdentifier(field.name))
                sql.append(' ')
                sql.append(DataTypeUtils.getHiveObjectInspector(field.dataType).getTypeName)
            }

            sql.append(") STORED AS ORC")
            sql.toString()
        }
    }

    /** Stores the `DataFrame` results in a [[com.thinkbiganalytics.discovery.schema.QueryResult]] and returns the object. */
    private class QueryResultCallable extends Callable[TransformResponse] {
        override def call(): TransformResponse = {
            // Cache data frame
            val cache = dataFrame.cache
            cache.registerTempTable(destination)

            // Build result object
            val result = new DefaultQueryResult("SELECT * FROM " + destination)

            val transform = new QueryResultRowTransform(cache.schema)
            result.setColumns(JavaConversions.seqAsJavaList(transform.columns))
            cache.collect().foreach(r => result.addRow(transform.apply(r)))

            // Build response object
            val response = new TransformResponse
            response.setResults(result)
            response.setStatus(TransformResponse.Status.SUCCESS)
            response.setTable(destination)

            response
        }
    }

    /** Transforms a Spark SQL `Row` into a [[com.thinkbiganalytics.discovery.schema.QueryResult]] row. */
    private object QueryResultRowTransform {
        /** Prefix for display names that are different from the field name */
        val DISPLAY_NAME_PREFIX = "col"

        /** Pattern for field names */
        val FIELD_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$")
    }

    private class QueryResultRowTransform(schema: StructType) extends (Row => util.HashMap[String, Object]) {
        /** Array of columns for the [[com.thinkbiganalytics.discovery.schema.QueryResult]] */
        val columns = {
            var index = 1
            schema.fields.map(field => {
                val column = new DefaultQueryResultColumn
                column.setDataType(DataTypeUtils.getHiveObjectInspector(field.dataType).getTypeName)
                column.setHiveColumnLabel(field.name)
                column.setTableName(destination)

                if (QueryResultRowTransform.FIELD_PATTERN.matcher(field.name).matches()) {
                    // Use original name if alphanumeric
                    column.setDisplayName(field.name)
                    column.setField(field.name)
                } else {
                    // Generate name for non-alphanumeric fields
                    var name: String = null
                    do {
                        name = QueryResultRowTransform.DISPLAY_NAME_PREFIX + index
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

                column
            })
        }

        /** Array of Spark SQL object to Hive object converters */
        val converters = schema.fields.map(field => DataTypeUtils.getHiveObjectConverter(field.dataType))

        override def apply(row: Row): util.HashMap[String, Object] = {
            val map = new util.HashMap[String, Object]()
            columns.indices.foreach(i => map.put(columns(i).getDisplayName, converters(i).convert(row.getAs(i))))
            map
        }
    }
}
