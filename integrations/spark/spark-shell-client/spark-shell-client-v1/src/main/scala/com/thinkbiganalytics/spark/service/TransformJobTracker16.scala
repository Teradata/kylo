package com.thinkbiganalytics.spark.service

import com.thinkbiganalytics.spark.repl.SparkScriptEngine

import org.apache.spark.Success
import org.apache.spark.scheduler._
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.annotation.Nonnull

/** Tracks the progress of executing and recently completed jobs.  */
@Component
class TransformJobTracker16 extends TransformJobTracker {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformJobTracker16])

    override def addSparkListener(engine: SparkScriptEngine): Unit = {
        engine.getSparkContext.addSparkListener(new SparkListener() {

            def onOtherEvent(@Nonnull event: SparkListenerEvent) {}

            /** Records the end of a job.
              *
              * @param event the job information
              */
            override def onJobEnd(@Nonnull event: SparkListenerJobEnd): Unit = {
                log.trace("Job {} ended", event.jobId)
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
                log.trace("Job {} started with stages {}", event.jobId, event.stageIds)

                // Find transform job ID
                val groupId = event.properties.getProperty(TransformJobTracker.SPARK_JOB_GROUP_ID)
                if (groupId == null) {
                    return
                }

                // Find transform job
                val job = groups.get(groupId)
                if (job.isEmpty) {
                    log.debug("Missing job {}", groupId)
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
                log.trace("Stage {} completed with failure {}", event.stageInfo.stageId, event.stageInfo.failureReason)
                val completed = if (event.stageInfo.failureReason.isEmpty) event.stageInfo.numTasks else 0
                stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, completed))
            }

            /** Records information about a stage when it starts.
              *
              * @param event the stage information
              */
            override def onStageSubmitted(@Nonnull event: SparkListenerStageSubmitted) {
                log.trace("Stage {} submitted with {} tasks", event.stageInfo.stageId, event.stageInfo.numTasks)
                stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, 0))
            }

            /** Records information about a task when it ends.
              *
              * @param event the task information
              */
            override def onTaskEnd(@Nonnull event: SparkListenerTaskEnd) {
                log.trace("Task {} completed for stage {} with {}", event.taskInfo.taskId.toString, event.stageId.toString, event.reason.toString)
                if (event.reason == Success) {
                    stages.get(event.stageId).foreach(_.onTaskEnd())
                }
            }
        })
    }
}

