package com.thinkbiganalytics.spark.service

import java.util.concurrent.{ExecutorService, Executors}
import javax.annotation.Nonnull

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.thinkbiganalytics.spark.metadata.TransformJob
import com.thinkbiganalytics.spark.repl.SparkScriptEngine

import scala.collection.mutable

/** Tracks the progress of executing and recently completed jobs.  */
object TransformJobTracker {

  /** Key for the job group ID property */
  val SPARK_JOB_GROUP_ID = "spark.jobGroup.id"
}

abstract class TransformJobTracker {

  /** Executes jobs in separate threads */
  private val executor: ExecutorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("transform-job-%d").build())

  /** Map of group id to job */
  protected val groups = new mutable.HashMap[String, TransformJob]() with mutable.SynchronizedMap[String, TransformJob]

  /** Map of job id to job */
  protected val jobs = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]

  /** Map of stage id to job */
  protected val stages = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]


  def addSparkListener(@Nonnull engine: SparkScriptEngine): Unit

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
