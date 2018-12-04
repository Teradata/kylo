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

import java.util

import com.thinkbiganalytics.kylo.spark.util.SparkUtil
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, RowFactory, SQLContext}

/**
  * Retreive file schema header for parquet, orc, avro, and xml files
  *
  * @param pathParam
  * @param mimeTypeParam
  * @param rowTagParam
  * @param userSchema
  * @param sqlContext
  */
class FileMetadataSchemaRelation protected[metadata](pathParam: Option[String],
                                                     mimeTypeParam: Option[String],
                                                     rowTagParam: Option[String],
                                                     userSchema: StructType)
                                                    (@transient val sqlContext: SQLContext)
    extends BaseRelation with TableScan with Serializable {

  override def schema: StructType = this.userSchema

  override def buildScan(): RDD[Row] = {
    var mimeType = mimeTypeParam.get
    var path = pathParam.get
    var rowTag = rowTagParam.get


    var schema: StructType = null;
    if (mimeType == "application/parquet" || mimeType == "parquet") {
      schema = sqlContext.read.option("samplingRatio", "0.1").parquet(path).schema
    }
    else if (mimeType == "application/avro" || mimeType == "avro") {
      schema = sqlContext.read.option("samplingRatio", "0.1").format("com.databricks.spark.avro").load(path).schema
    }
    else if (mimeType == "application/orc" || mimeType == "orc") {
      var schema = sqlContext.read.option("samplingRatio", "0.1").format("orc").load(path).schema
    }
    else if (mimeType == "application/xml" || mimeType == "xml") {
      schema = sqlContext.read.option("rowTag", rowTag).format("xml").load(path).schema
    }
    else if (mimeType == "application/json" || mimeType == "json") {
      schema = sqlContext.read.option("samplingRatio", "1").format("json").load(path).schema
    }
    else {
      schema = sqlContext.read.option("samplingRatio", "1").format("text").load(path).schema
    }
    var schemaMap: java.util.Map[String, String] = new util.HashMap[String, String]()
    if (schema != null) {
      schema.foreach(s => schemaMap.put(s.name, s.dataType.toString))
    }

    val scalaSchemaMap = SparkUtil.toScalaMap(schemaMap)

    val row: Row = RowFactory.create(path, mimeType, scalaSchemaMap)


    sqlContext.sparkContext.makeRDD(List(row))

  }


}