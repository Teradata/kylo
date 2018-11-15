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

import com.thinkbiganalytics.kylo.spark.util.SparkUtil
import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, RowFactory, SQLContext}

/**
  * Calls the FileMetadataExtractor to get mimetype information for the incoming file
  *
  * @param path
  * @param userSchema
  * @param metadataExtractor
  * @param sqlContext
  */
class FileMetadataRelation protected[metadata](path: Option[String],
                                               userSchema: StructType,
                                               metadataExtractor: MetadataExtractor)
                                              (@transient val sqlContext: SQLContext)
    extends BaseRelation with TableScan with Serializable {

  override def schema: StructType = this.userSchema

  override def buildScan(): RDD[Row] = {

    val rdd = sqlContext.sparkContext.binaryFiles(path.get)
    rdd.map(extractFunc(_))

  }


  def extractFunc(file: (String, PortableDataStream)): Row = {
    val extractedData = metadataExtractor.extract(file)


    val resourceName = extractedData.getProperties.get("resourceName")
    //for known mimetypes attempt to get the header
    val mimeType = extractedData.getMimeType;


    val propertyMap = SparkUtil.toScalaMap(extractedData.getProperties)

    return RowFactory.create(resourceName, extractedData.getMimeType, extractedData.getSubType, extractedData.getEncoding, propertyMap)
  }


}