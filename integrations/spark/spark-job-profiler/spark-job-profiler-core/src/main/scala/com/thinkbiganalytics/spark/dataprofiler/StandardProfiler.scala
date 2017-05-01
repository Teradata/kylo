package com.thinkbiganalytics.spark.dataprofiler

import com.thinkbiganalytics.spark.dataprofiler.function.PartitionLevelModels
import com.thinkbiganalytics.spark.{DataSet, SparkContextService}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructField

/** The standard implementation of `Profiler` that uses Spark to analyze the columns.
  *
  * @param sqlContext          the Spark SQL context
  * @param sparkContextService the Spark context service
  */
class StandardProfiler(val sqlContext: SQLContext, val sparkContextService: SparkContextService) extends Profiler {
    override def profile(dataset: DataSet, profilerConfiguration: ProfilerConfiguration): StatisticsModel = {
        /* Update schema map and broadcast it*/
        val schemaMap = populateSchemaMap(dataset)

        /* Get profile statistics */
        profileStatistics(dataset, schemaMap, profilerConfiguration).orNull
    }

    /** Generates a map from column index to field type.
      *
      * @param dataset the data set
      * @return the schema map
      */
    private def populateSchemaMap(dataset: DataSet): Map[Int, StructField] = {
        dataset.schema().fields.zipWithIndex.map(tuple => (tuple._2, tuple._1)).toMap
    }

    /** Profiles the columns in the specified data set.
      *
      * @param dataset   the data set
      * @param schemaMap the schema map
      * @return the statistics model
      */
    private def profileStatistics(dataset: DataSet, schemaMap: Map[Int, StructField], profilerConfiguration: ProfilerConfiguration): Option[StatisticsModel] = {
        // Get ((column index, column value), count)
        val columnValueCounts = dataset.rdd
            .flatMap((row) => row.toSeq.zipWithIndex.map((tuple) => ((tuple._2, tuple._1), 1)))
            .reduceByKey((a, b) => a + b)

        // Generate the profile model
        val partitionLevelModels = columnValueCounts.mapPartitions(new PartitionLevelModels(schemaMap, profilerConfiguration))
        if (!partitionLevelModels.isEmpty) {
            Option(partitionLevelModels.reduce((a, b) => {
                a.combine(b)
                a
            }))
        } else {
            Option.empty
        }
    }
}
