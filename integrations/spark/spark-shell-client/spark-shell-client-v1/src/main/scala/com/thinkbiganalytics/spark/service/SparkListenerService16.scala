package com.thinkbiganalytics.spark.service

import javax.annotation.Nonnull

import com.thinkbiganalytics.spark.metadata.StandardSparkListener
import org.apache.spark.SparkContext
import org.apache.spark.scheduler._

/** Implementation of [[SparkListenerService]] for Spark 1.6. */
class SparkListenerService16(private val sc: SparkContext) extends SparkListenerService {

    def addSparkListener(@Nonnull listener: StandardSparkListener): Unit =
        sc.addSparkListener(new SparkListener16(listener))
}

/** Implementation of [[SparkListener]] for Spark 1.6.
  *
  * Do not make this an anonymous class. The onOtherEvent method must be public to work with CDH 5.8+.
  */
private class SparkListener16(private val listener: StandardSparkListener) extends SparkListener {

    override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = listener.onStageCompleted(stageCompleted)

    override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = listener.onStageSubmitted(stageSubmitted)

    override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = listener.onTaskEnd(taskEnd)

    override def onJobStart(jobStart: SparkListenerJobStart): Unit = listener.onJobStart(jobStart)

    override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = listener.onJobEnd(jobEnd)

    def onOtherEvent(event: SparkListenerEvent) {}
}
