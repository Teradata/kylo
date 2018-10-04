package com.thinkbiganalytics.spark.utils

import com.thinkbiganalytics.spark.logger.LivyLogger
import org.slf4j.LoggerFactory
import org.apache.spark.sql.types.StructType

object LivyPaginator {
  val logger = LoggerFactory.getLogger(LivyPaginator.getClass)

  def page(df: org.apache.spark.sql.DataFrame, startCol: Int, stopCol: Int, pageStart: Int, pageStop: Int): List[Any] = {
    var dfRows: List[Object] = List()
    var actualCols: Integer = 0
    var actualRows: Int = 0

    LivyLogger.time {
      actualCols = df.columns.length

      val schema = StructType(df.schema.slice(startCol, stopCol));
      val dfAsArray = df.collect

      actualRows = dfAsArray.size

      val dfStartRow = if (actualRows >= pageStart) pageStart else actualRows
      val dfStopRow = if (actualRows >= pageStop) pageStop else actualRows

      val pagedRows = dfAsArray.slice(dfStartRow, dfStopRow)  // slice the rows
          .map(_.toSeq.slice(startCol, stopCol)) // slice the columns

      dfRows = List(schema.json, pagedRows)
    }
    return List(dfRows, actualCols, actualRows)
  }
}
