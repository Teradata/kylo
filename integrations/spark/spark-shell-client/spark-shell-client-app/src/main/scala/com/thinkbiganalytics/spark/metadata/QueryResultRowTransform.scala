package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.regex.Pattern

import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult
import com.thinkbiganalytics.spark.util.DataTypeUtils
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

/** Helper for building [[TransformQueryResult]] objects from a Spark dataset. */
object QueryResultRowTransform {
    /** Prefix for display names that are different from the field name */
    val DISPLAY_NAME_PREFIX = "col"

    /** Pattern for field names */
    val FIELD_PATTERN: Pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$")
}

class QueryResultRowTransform(schema: StructType, destination: String) {
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

    /** Adds the specified row to the query result. */
    def addRow(row: Row, result: TransformQueryResult): Unit = {
        result.addRow(getQueryResultRow(row))
    }

    /** Coverts the specified row into a [[TransformQueryResult]] compatible object. */
    protected def getQueryResultRow(row: Row): util.Map[String, Object] = {
        val map = new util.HashMap[String, Object]()
        columns.indices.foreach(i => map.put(columns(i).getDisplayName, converters(i).convert(row.getAs(i))))
        map
    }
}
