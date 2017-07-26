package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.concurrent.Callable
import java.util.regex.Pattern

import com.thinkbiganalytics.discovery.model.{DefaultQueryResult, DefaultQueryResultColumn}
import com.thinkbiganalytics.discovery.schema.{QueryResult, QueryResultColumn}
import com.thinkbiganalytics.spark.DataSet
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow
import com.thinkbiganalytics.spark.dataprofiler.{Profiler, ProfilerConfiguration}
import com.thinkbiganalytics.spark.rest.model.TransformResponse
import com.thinkbiganalytics.spark.util.DataTypeUtils
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/** Wraps a transform script into a function that can be evaluated.
  *
  * @param destination the name of the destination Hive table
  */
abstract class TransformScript(destination: String, profiler: Profiler) {

    /** Evaluates the transform script.
      *
      * @return the transformation result
      */
    protected[metadata] def dataFrame: Object

    /** Fetches or re-generates the results of the parent transformation, if available.
      *
      * @return the parent results
      */
    protected def parent: Object

    /** Re-generates the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentDataFrame: Object = {
        throw new UnsupportedOperationException
    }

    /** Gets the name of the Hive table with the results of the parent transformation.
      *
      * @return the parent transformation
      */
    protected def parentTable: String = {
        throw new UnsupportedOperationException
    }

    /** Stores the `DataFrame` results in a [[QueryResultColumn]] and returns the object. */
    protected abstract class QueryResultCallable extends Callable[TransformResponse] {

        /** Builds a response model from a data set result.
          *
          * @param dataset the data set
          * @return the response model
          */
        private[metadata] def toResponse(dataset: DataSet) = {
            // Build the result set
            val result = new DefaultQueryResult("SELECT * FROM " + destination)

            val transform = new QueryResultRowTransform(dataset.schema())
            result.setColumns(transform.columns.toSeq)
            for (row <- dataset.collectAsList()) {
                result.addRow(transform.apply(row))
            }

            // Generate the column statistics
            val profile: Option[util.List[OutputRow]] = Option(profiler)
                .flatMap(p => Option(p.profile(dataset, new ProfilerConfiguration)))
                .map(_.getColumnStatisticsMap.asScala)
                .map(_.flatMap(_._2.getStatistics))
                .map(_.toSeq)

            // Build the response
            val response = new TransformResponse
            response.setProfile(profile.orNull)
            response.setResults(result)
            response.setStatus(TransformResponse.Status.SUCCESS)
            response.setTable(destination)
            response
        }
    }

    /** Transforms a Spark SQL `Row` into a [[QueryResult]] row. */
    private object QueryResultRowTransform {
        /** Prefix for display names that are different from the field name */
        val DISPLAY_NAME_PREFIX = "col"

        /** Pattern for field names */
        val FIELD_PATTERN: Pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$")
    }

    private class QueryResultRowTransform(schema: StructType) extends (Row => util.HashMap[String, Object]) {
        /** Array of columns for the [[com.thinkbiganalytics.discovery.schema.QueryResultColumn]] */
        val columns: Array[DefaultQueryResultColumn] = {
            var index = 1
            schema.fields.map(field => {
                val column = new DefaultQueryResultColumn
                column.setComment(if (field.metadata.contains("comment")) field.metadata.getString("comment") else null)
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
                            case _: IllegalArgumentException => // ignored
                        }
                    } while (name == null)

                    column.setDisplayName(name)
                    column.setField(name)
                }

                column
            })
        }

        /** Array of Spark SQL object to Hive object converters */
        val converters: Array[ObjectInspectorConverters.Converter] = schema.fields.map(field => DataTypeUtils.getHiveObjectConverter(field.dataType))

        override def apply(row: Row): util.HashMap[String, Object] = {
            val map = new util.HashMap[String, Object]()
            columns.indices.foreach(i => map.put(columns(i).getDisplayName, converters(i).convert(row.getAs(i))))
            map
        }
    }

}
