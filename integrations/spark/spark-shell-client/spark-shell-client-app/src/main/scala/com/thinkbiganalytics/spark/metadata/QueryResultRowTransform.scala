package com.thinkbiganalytics.spark.metadata

import java.util
import java.util.regex.Pattern

import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult
import com.thinkbiganalytics.spark.service.DataSetConverterService
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import scala.collection.mutable.HashSet
import scala.collection.JavaConverters._


/** Helper for building [[TransformQueryResult]] objects from a Spark dataset. */
object QueryResultRowTransform {
    /** Prefix for display names that are different from the field name */
    val DISPLAY_NAME_PREFIX = "col"

    /** Pattern for field names */
    val FIELD_PATTERN: Pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$")

    /** String pattern for replacing illegal characters **/
    val REPLACE_PATTERN_STR = "[^a-zA-Z0-9_]+"
}

class QueryResultRowTransform(schema: StructType, destination: String, converterService: DataSetConverterService) {
    /** Array of columns for the [[com.thinkbiganalytics.discovery.schema.QueryResultColumn]] */

    val columns: Array[DefaultQueryResultColumn] = {

        val uniqueColumns = new HashSet[String]

        schema.fields.map(field => {
            val column = new DefaultQueryResultColumn
            column.setComment(if (field.metadata.contains("comment")) field.metadata.getString("comment") else null)
            column.setDataType(converterService.getHiveObjectInspector(field.dataType).getTypeName)
            column.setHiveColumnLabel(field.name)
            column.setTableName(destination)
            uniqueColumns.add(field.name)

            if (QueryResultRowTransform.FIELD_PATTERN.matcher(field.name).matches()) {
                // Use original name if alphanumeric
                column.setDisplayName(field.name)
                column.setField(field.name)
            } else {
                var name = uniqueName(uniqueColumns, field.name)
                column.setDisplayName(name)
                column.setField(name)
                uniqueColumns.add(name)
            }
            column
        })
    }

    /** Generate a valid, unique column name that preserves semantic quality **/
    def uniqueName(uniqueColumns:HashSet[String], fieldName:String) : String = {
      var newIndex = uniqueColumns.size + 1
      // Replace illegal with underscores
      var name: String =  if (fieldName != null && fieldName.length > 0) fieldName.replaceAll(QueryResultRowTransform.REPLACE_PATTERN_STR, "_") else null
      // If starts with number, prefix the name
      name = if (name != null && name.substring(0, 1).matches("\\d")) "c_"+name else name
      // Strip underscore ending
      name = if (name != null && name.endsWith("_")) name.substring(0, name.length -1) else name
      // Generate random name if needed
      name = if (name == null || name.length == 0) QueryResultRowTransform.DISPLAY_NAME_PREFIX + newIndex else name
      // Force unique but preserve root
      var i = 1;
      while (uniqueColumns.contains(name)) {
        var tryName = name + "_" + i;
        name = if (!uniqueColumns.contains(tryName)) tryName else name
          i += 1
      }
      name
    }

    /** Array of Spark SQL object to Hive object converters */
    val converters: Array[ObjectInspectorConverters.Converter] = schema.fields.map(field => converterService.getHiveObjectConverter(field.dataType))

    /** Coverts the specified row into a [[TransformQueryResult]] compatible object. */
    def convertRow(row: Row): util.List[Object] = {
        columns.indices.map(i => converters(i).convert(row.getAs(i))).asJava
    }

   /** Coverts the specified row into a [[TransformQueryResult]] compatible object. */
    def convertPagedRow(row: Row, columnIndices: List[Int]): util.List[Object] = {
        columnIndices.map(i => converters(i).convert(row.getAs(i))).asJava
    }

}
