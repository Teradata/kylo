package com.thinkbiganalytics.spark.dataprofiler.function

import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration
import com.thinkbiganalytics.spark.dataprofiler.model.StandardStatisticsModel
import org.apache.spark.sql.types.StructField

/** Creates a statistics model from RDD values.
  *
  * @param schemaMap the schema map
  */
class PartitionLevelModels(val schemaMap: Map[Int, StructField], val profilerConfiguration: ProfilerConfiguration) extends (Iterator[((Int, Any), Int)] => Iterator[StandardStatisticsModel])
    with Serializable {

    override def apply(iter: Iterator[((Int, Any), Int)]): Iterator[StandardStatisticsModel] = {
        val statisticsModel = new StandardStatisticsModel(profilerConfiguration)

        for ((k, v) <- iter) {
            statisticsModel.add(k._1, k._2, v.toLong, schemaMap(k._1))
        }

        Iterator.apply(statisticsModel)
    }
}
