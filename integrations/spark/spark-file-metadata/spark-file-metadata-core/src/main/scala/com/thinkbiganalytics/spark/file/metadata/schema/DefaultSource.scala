package com.thinkbiganalytics.spark.file.metadata.schema

/*-
 * #%L
 * spark-file-metadata-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, SchemaRelationProvider}
import org.apache.spark.sql.types.{DataTypes, StringType, StructField, StructType}

/**
  * Retreive file schema header for parquet, orc, avro, and xml files
  * Example usage:
  * var df = sqlContext.read.option("mimeType","application/parquet").format("com.thinkbiganalytics.spark.file.metadata.schema").load("file:/opt/kylo/setup/data/sample-data/parquet/userdata1.parquet")
  *
  * var df2 = sqlContext.read.option("mimeType","application/avro").format("com.thinkbiganalytics.spark.file.metadata.schema").load("file:/opt/kylo/setup/data/sample-data/avro/userdata1.avro")
  *
  * This returns a RDD with Row of StructType of String, String, Map[String,String]  (column, dataType)
  */
class DefaultSource
    extends RelationProvider with SchemaRelationProvider with DataSourceRegister {


  private def checkPath(parameters: Map[String, String]): String = {
    parameters.getOrElse("path", sys.error("'path' must be specified for data."))
  }


  def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {

    val path = checkPath(parameters)
    val mimeType = parameters.getOrElse("mimeType", "'mimeType' must be specified for data")
    var rowTag = ""
    if (mimeType == "xml" || mimeType == "application/xml") {
      rowTag = parameters.getOrElse("rowTag", "'rowTag' must be specified for xml data")
    }

    new FileMetadataSchemaRelation(
      Some(path),
      Some(mimeType),
      Some(rowTag),
      schema
    )(sqlContext)
  }


  override def shortName(): String = "file-metadata-schema"

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    val struct =
      StructType(
        StructField("resource", StringType, true) ::
            StructField("mimeType", StringType, true) ::
            StructField("schema", DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType), true) :: Nil
      )
    createRelation(sqlContext, parameters, struct)
  }


}