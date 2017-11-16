package com.thinkbiganalytics.spark.service

import javax.annotation.Nonnull

import com.thinkbiganalytics.spark.repl.SparkScriptEngine
import org.apache.spark.Success
import org.apache.spark.scheduler._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/** Tracks the progress of executing and recently completed jobs.  */
class TransformJobTracker20(contextClassLoader: ClassLoader) extends TransformJobTracker(contextClassLoader) {

    private[this] val log = LoggerFactory.getLogger(classOf[TransformJobTracker20])

    override def addSparkListener(engine: SparkScriptEngine): Unit = {
        engine.getSparkContext.addSparkListener(new SparkListener() {

            override def onOtherEvent(@Nonnull event: SparkListenerEvent) {}

            /** Records the end of a job.
              *
              * @param event the job information
              */
            override def onJobEnd(@Nonnull event: SparkListenerJobEnd): Unit = {
                log.trace("Job {} ended", event.jobId)
                val job = jobs.remove(event.jobId)
                if (job != null) {
                    job.onJobEnd()
                    for (stage: StageInfo <- job.getStages.asScala) {
                        stages.remove(stage.stageId)
                    }
                }
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
                val job = getJob(groupId)
                if (!job.isPresent) {
                    log.debug("Missing job {}", groupId)
                    return
                }

                // Update job info
                job.get.setStages(event.stageInfos.asJava)
                jobs.put(event.jobId, job.get)
                event.stageIds.foreach(stages.put(_, job.get))
            }

            /** Records information about a completed stage.
              *
              * @param event the stage information
              */
            override def onStageCompleted(@Nonnull event: SparkListenerStageCompleted) {
                log.trace("Stage {} completed with failure {}", event.stageInfo.stageId, event.stageInfo.failureReason)
                val completed = if (event.stageInfo.failureReason.isEmpty) event.stageInfo.numTasks else 0
                Option(stages.get(event.stageInfo.stageId)).foreach(_.onStageProgress(event.stageInfo, completed))
            }

            /** Records information about a stage when it starts.
              *
              * @param event the stage information
              */
            override def onStageSubmitted(@Nonnull event: SparkListenerStageSubmitted) {
                log.trace("Stage {} submitted with {} tasks", event.stageInfo.stageId, event.stageInfo.numTasks)
                Option(stages.get(event.stageInfo.stageId)).foreach(_.onStageProgress(event.stageInfo, 0))
            }

            /** Records information about a task when it ends.
              *
              * @param event the task information
              */
            override def onTaskEnd(@Nonnull event: SparkListenerTaskEnd) {
                log.trace("Task {} completed for stage {} with {}", event.taskInfo.taskId.toString, event.stageId.toString, event.reason.toString)
                if (event.reason == Success) {
                    Option(stages.get(event.stageId)).foreach(_.onTaskEnd())
                }
            }
        })
    }
}

