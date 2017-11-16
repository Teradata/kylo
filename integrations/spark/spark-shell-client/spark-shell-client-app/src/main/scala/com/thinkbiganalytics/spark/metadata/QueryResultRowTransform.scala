package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.regex.Pattern

import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult
import com.thinkbiganalytics.spark.util.DataTypeUtils
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.collection.JavaConverters._

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

    /** Coverts the specified row into a [[TransformQueryResult]] compatible object. */
    def convertRow(row: Row): util.List[Object] = {
        columns.indices.map(i => converters(i).convert(row.getAs(i))).asJava
    }
}
