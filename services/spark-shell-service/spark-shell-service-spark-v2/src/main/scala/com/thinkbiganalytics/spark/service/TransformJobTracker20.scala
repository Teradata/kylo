package com.thinkbiganalytics.spark.service

import javax.annotation.Nonnull

import com.thinkbiganalytics.spark.repl.SparkScriptEngine
import org.apache.spark.Success
import org.apache.spark.scheduler._
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Tracks the progress of executing and recently completed jobs.  */
object TransformJobTracker20 {

  private val log = LoggerFactory.getLogger(classOf[TransformJobTracker20])

}

@Component
class TransformJobTracker20 extends TransformJobTracker {

  override def addSparkListener(engine: SparkScriptEngine): Unit = {
    engine.getSparkContext.addSparkListener(new SparkListener() {

      override def onOtherEvent(@Nonnull event: SparkListenerEvent) {}

      /** Records the end of a job.
        *
        * @param event the job information
        */
      override def onJobEnd(@Nonnull event: SparkListenerJobEnd): Unit = {
        TransformJobTracker20.log.trace("Job {} ended", event.jobId)
        jobs.remove(event.jobId).foreach((job) => {
          job.onJobEnd()
          job.stages.map(_.stageId).foreach(stages.remove)
        })
      }

      /** Records information about a job when it starts.
        *
        * @param event the job information
        */
      override def onJobStart(@Nonnull event: SparkListenerJobStart) {
        TransformJobTracker20.log.trace("Job {} started with stages {}", event.jobId, event.stageIds)

        // Find transform job ID
        val groupId = event.properties.getProperty(TransformJobTracker.SPARK_JOB_GROUP_ID)
        if (groupId == null) {
          return
        }

        // Find transform job
        val job = groups.get(groupId)
        if (job.isEmpty) {
          TransformJobTracker20.log.debug("Missing job {}", groupId)
          return
        }

        // Update job info
        job.get.stages = event.stageInfos
        jobs.update(event.jobId, job.get)
        event.stageIds.foreach(stages.update(_, job.get))
      }

      /** Records information about a completed stage.
        *
        * @param event the stage information
        */
      override def onStageCompleted(@Nonnull event: SparkListenerStageCompleted) {
        TransformJobTracker20.log.trace("Stage {} completed with failure {}", event.stageInfo.stageId, event.stageInfo.failureReason)
        val completed = if (event.stageInfo.failureReason.isEmpty) event.stageInfo.numTasks else 0
        stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, completed))
      }

      /** Records information about a stage when it starts.
        *
        * @param event the stage information
        */
      override def onStageSubmitted(@Nonnull event: SparkListenerStageSubmitted) {
        TransformJobTracker20.log.trace("Stage {} submitted with {} tasks", event.stageInfo.stageId, event.stageInfo.numTasks)
        stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, 0))
      }

      /** Records information about a task when it ends.
        *
        * @param event the task information
        */
      override def onTaskEnd(@Nonnull event: SparkListenerTaskEnd) {
        TransformJobTracker20.log.trace("Task {} completed for stage {} with {}", event.taskInfo.taskId.toString, event.stageId.toString, event.reason.toString)
        if (event.reason == Success) {
          stages.get(event.stageId).foreach(_.onTaskEnd())
        }
      }

    })
  }
}

