package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.spark.rest.model.TransformResponse

import org.apache.spark.SparkContext
import org.apache.spark.scheduler.StageInfo

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Callable, FutureTask}

/** A Spark transformation job.
  *
  * This class is thread-safe but `run` should only be invoked once.
  *
  * @param groupId      the group id
  * @param callable     the transformation function
  * @param sparkContext the Spark context
  */
class TransformJob(val groupId: String, private val callable: Callable[TransformResponse], private val sparkContext: SparkContext) extends FutureTask[TransformResponse](callable) {

    /** Spark job id */
    var jobId = Option(0)

    /** List of Spark stages */
    private var _stages: Seq[StageInfo] = Seq()

    /** Number of completed tasks */
    private val tasksCompleted: AtomicInteger = new AtomicInteger(0)

    /** Total number of tasks */
    private val tasksTotal: AtomicInteger = new AtomicInteger(0)

    /** Sets the job progress to completed. */
    def onJobEnd(): Unit = {
        tasksCompleted.set(tasksTotal.get())
    }

    /** Sets the job progress for the specified stage.
      *
      * @param stage          the current stage
      * @param tasksCompleted the number of completed tasks
      */
    def onStageProgress(stage: StageInfo, tasksCompleted: Int): Unit = {
        this.tasksCompleted.set(stages.filter(_.stageId < stage.stageId).map(_.numTasks).sum + tasksCompleted)
    }

    /** Sets a task progress to completed. */
    def onTaskEnd(): Unit = {
        tasksCompleted.incrementAndGet()
    }

    /** Gets the progress of this transformation
      *
      * @return the progress from 0.0 to 1.0
      */
    def progress: Double = {
        val total = tasksTotal.get()
        if (total > 0) {
            tasksCompleted.get().toDouble / total
        } else {
            0.0
        }
    }

    override def run() {
        sparkContext.setJobGroup(groupId, "Transform Job", interruptOnCancel = false)
        super.run()
        sparkContext.clearJobGroup
    }

    override protected def runAndReset: Boolean = {
        throw new UnsupportedOperationException
    }

    /** Gets the Spark stages. */
    def stages = _stages

    /** Sets the Spark stages. */
    def stages_=(stages: Seq[StageInfo]) = {
        _stages = stages
        tasksCompleted.set(0)
        tasksTotal.set(stages.map(_.numTasks).sum)
    }
}
