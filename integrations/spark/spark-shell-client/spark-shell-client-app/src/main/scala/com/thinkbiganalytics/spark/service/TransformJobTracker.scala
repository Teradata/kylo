package com.thinkbiganalytics.spark.service

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import javax.annotation.Nonnull

import com.google.common.cache.{Cache, CacheBuilder, RemovalListener, RemovalNotification}
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
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("transform-job-%d").build())

    /** Map of group id to job */
    protected val groups: Cache[String, TransformJob] = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .removalListener(new RemovalListener[String, TransformJob] {
            override def onRemoval(notification: RemovalNotification[String, TransformJob]): Unit = {
                notification.getValue.jobId.foreach(jobs.remove)
                notification.getValue.stages.map(_.stageId).foreach(stages.remove)
            }
        })
        .build[String, TransformJob]()

    // Schedule clean-up of groups
    executor.scheduleAtFixedRate(new Runnable {
        override def run(): Unit = groups.cleanUp()
    }, 1, 1, TimeUnit.HOURS)

    /** Map of job id to job */
    protected val jobs = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]

    /** Map of stage id to job */
    protected val stages = new mutable.HashMap[Int, TransformJob]() with mutable.SynchronizedMap[Int, TransformJob]

    /** Adds a listener to the Spark context of the specified script engine.
      *
      * @param engine the Spark script engine
      */
    def addSparkListener(@Nonnull engine: SparkScriptEngine): Unit

    /** Gets the job with the specified group id.
      *
      * @param groupId the group id
      * @return the job
      * @throws IllegalArgumentException if the job does not exist
      */
    @Nonnull
    def getJob(@Nonnull groupId: String): Option[TransformJob] = {
        Option(groups.getIfPresent(groupId))
    }

    /** Removes the specified job from this tracker.
      *
      * @param groupId the group id
      */
    def removeJob(@Nonnull groupId: String): Unit = {
        groups.invalidate(groupId)
    }

    /** Submits a job to be executed.
      *
      * @param job the transform job
      */
    def submitJob(@Nonnull job: TransformJob): Unit = {
        groups.put(job.groupId, job)
        executor.execute(job)
    }
}
