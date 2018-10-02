package com.thinkbiganalytics.spark.logger

import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder
import org.slf4j.LoggerFactory

object LivyLogger {
  val logger = LoggerFactory.getLogger(LivyLogger.getClass)

  final val NUM_NANOS_IN_MILLIS = 1000 * 1000;

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    logger.warn("Elapsed time: " + prettyDuration(t1 - t0))
    result
  }

  def prettyDuration(nanos : Long) = {
    val duration = new Duration(java.lang.Long.valueOf(nanos/ NUM_NANOS_IN_MILLIS)); // in milliseconds
    val formatter = new PeriodFormatterBuilder()
      .appendDays()
      .appendSuffix("d")
      .appendHours()
      .appendSuffix("h")
      .appendMinutes()
      .appendSuffix("m")
      .appendSeconds()
      .appendSuffix("s")
      .appendMillis()
      .appendSuffix("ms")
      .toFormatter();
    val formatted = formatter.print(duration.toPeriod());
    formatted;
  }
}
