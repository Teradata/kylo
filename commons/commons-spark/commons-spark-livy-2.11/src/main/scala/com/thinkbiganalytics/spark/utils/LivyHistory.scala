package com.thinkbiganalytics.spark.utils

import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

import org.apache.spark.sql.DataFrame;

//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;

object LivyHistory {
  val logger = LoggerFactory.getLogger(LivyHistory.getClass)

  def readOrExecute(sqlContext: SQLContext, tableId: String, history: () =>  org.apache.spark.sql.DataFrame):  org.apache.spark.sql.DataFrame = {
    return try {
      val df = sqlContext.read.table(tableId)
      logger.info("Parent dataframe set to table '{}'", tableId);
      df
    } catch {
      case _: Exception => {
        logger.info("Unable to load data from parent table '{}'.  Rebuild from transformation history.", tableId);
        history()
      }
    }
  }
}
