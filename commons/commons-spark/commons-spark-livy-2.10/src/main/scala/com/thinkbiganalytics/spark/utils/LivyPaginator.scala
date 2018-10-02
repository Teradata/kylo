package com.thinkbiganalytics.spark.utils

import com.thinkbiganalytics.spark.logger.LivyLogger
import org.apache.spark.sql.functions.col
import org.slf4j.LoggerFactory

object LivyPaginator {
  val logger = LoggerFactory.getLogger(LivyPaginator.getClass)

  def page(df: org.apache.spark.sql.DataFrame, startCol: Int, stopCol: Int, pageStart: Int, pageStop: Int): List[Any] = {
    var dfRows: List[Object] = List()
    var actualCols: Integer = 0
    var actualRows: Int = 0

    LivyLogger.time {
      actualCols = df.columns.length

      val lastCol = actualCols - 1
      val dfStartCol = if (lastCol >= startCol) startCol else lastCol
      val dfStopCol = if (lastCol >= stopCol) stopCol else lastCol

      val df2 = df.select(dfStartCol to dfStopCol map df.columns map col: _*)
      val dl = df2.collect

      actualRows = dl.size

      df.columns.slice(dfStartCol, dfStopCol)

      val dfStartRow = if (actualRows >= pageStart) pageStart else actualRows
      val dfStopRow = if (actualRows >= pageStop) pageStop else actualRows

      val pagedRows = dl.slice(dfStartRow, dfStopRow).map(_.toSeq)
      dfRows = List(df2.schema.json, pagedRows)
    }
    return List(dfRows, actualCols, actualRows)
  }
}
