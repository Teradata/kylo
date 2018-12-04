package com.thinkbiganalytics.spark.file.metadata

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
  * extract file metadata from a list of incoming files
  *
  * Returns a RDD Row of StructType String, String, String, String, Map[String,String]
  */
class DefaultSource
    extends RelationProvider with SchemaRelationProvider with DataSourceRegister {


  private def checkPath(parameters: Map[String, String]): String = {
    parameters.getOrElse("path", sys.error("'path' must be specified for data."))
  }


  def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {

    val path = checkPath(parameters)

    new FileMetadataRelation(
      Some(path),
      schema,
      new MetadataExtractor()
    )(sqlContext)
  }


  override def shortName(): String = "file-metadata"

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    val struct =
      StructType(
        StructField("resource", StringType, true) ::
            StructField("mimeType", StringType, true) ::
            StructField("subType", StringType, true) ::
            StructField("encoding", StringType, true) ::
            StructField("properties", DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType), true) :: Nil
      )
    createRelation(sqlContext, parameters, struct)
  }


}