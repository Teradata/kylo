package com.thinkbiganalytics.spark.service

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.thinkbiganalytics.spark.metadata.TransformJob

import org.apache.spark.Success
import org.apache.spark.scheduler._
import org.slf4j.LoggerFactory

import java.util.concurrent.{ExecutorService, Executors}
import javax.annotation.Nonnull

import scala.collection.mutable

/** Tracks the progress of executing and recently completed jobs.  */
object TransformJobTracker {

    private val log = LoggerFactory.getLogger(classOf[TransformJobTracker])

    /** Key for the job group ID property */
    private val SPARK_JOB_GROUP_ID = "spark.jobGroup.id"
}

class TransformJobTracker extends SparkListener {

    /** Executes jobs in separate threads */
    private val executor: ExecutorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("transform-job-%d").build())

    /** Map of group id to job */
    private val groups = new mutable.HashMap[String, TransformJob]() with mutable.SynchronizedMap[String, TransformJob]

    /** Map of job id to job */
    private val jobs = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]

    /** Map of stage id to job */
    private val stages = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]

    /** Gets the job with the specified group id.
      *
      * @param groupId the group id
      * @return the job
      * @throws IllegalArgumentException if the job does not exist
      */
    @Nonnull
    def getJob(@Nonnull groupId: String): Option[TransformJob] = {
        groups.get(groupId)
    }

    /** Records the end of a job.
      *
      * @param event the job information
      */
    override def onJobEnd(@Nonnull event: SparkListenerJobEnd): Unit = {
        TransformJobTracker.log.trace("Job {} ended", event.jobId)
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
        TransformJobTracker.log.trace("Job {} started with stages {}", event.jobId, event.stageIds)

        // Find transform job ID
        val groupId = event.properties.getProperty(TransformJobTracker.SPARK_JOB_GROUP_ID)
        if (groupId == null) {
            return
        }

        // Find transform job
        val job = groups.get(groupId)
        if (job.isEmpty) {
            TransformJobTracker.log.debug("Missing job {}", groupId)
            return
        }

        // Update job info
        job.get.stages = event.stageInfos
        jobs.update(event.jobId, job.get)
        event.stageIds.foreach(stages.update(_, job.get))
    }

    /** Handles unsupported events.
      *
      * Required for compatibility with CDH 5.8.
      *
      * @param event the spark event
      */
    def onOtherEvent(@Nonnull event: SparkListenerEvent) {}

    /** Records information about a completed stage.
      *
      * @param event the stage information
      */
    override def onStageCompleted(@Nonnull event: SparkListenerStageCompleted) {
        TransformJobTracker.log.trace("Stage {} completed with failure {}", event.stageInfo.stageId, event.stageInfo.failureReason)
        val completed = if (event.stageInfo.failureReason.isEmpty) event.stageInfo.numTasks else 0
        stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, completed))
    }

    /** Records information about a stage when it starts.
      *
      * @param event the stage information
      */
    override def onStageSubmitted(@Nonnull event: SparkListenerStageSubmitted) {
        TransformJobTracker.log.trace("Stage {} submitted with {} tasks", event.stageInfo.stageId, event.stageInfo.numTasks)
        stages.get(event.stageInfo.stageId).foreach(_.onStageProgress(event.stageInfo, 0))
    }

    /** Records information about a task when it ends.
      *
      * @param event the task information
      */
    override def onTaskEnd(@Nonnull event: SparkListenerTaskEnd) {
        TransformJobTracker.log.trace("Task {} completed for stage {} with {}", event.taskInfo.taskId.toString, event.stageId.toString, event.reason.toString)
        if (event.reason == Success) {
            stages.get(event.stageId).foreach(_.onTaskEnd())
        }
    }

    /** Removes the specified job from this tracker.
      *
      * @param groupId the group id
      */
    def removeJob(@Nonnull groupId: String): Unit = {
        groups.remove(groupId).foreach((job) => {
            job.jobId.foreach(jobs.remove)
            job.stages.map(_.stageId).foreach(stages.remove)
        })
    }

    /** Submits a job to be executed.
      *
      * @param job the transform job
      */
    def submitJob(@Nonnull job: TransformJob): Unit = {
        groups(job.groupId) = job
        executor.execute(job)
    }
}
