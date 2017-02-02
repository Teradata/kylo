package com.thinkbiganalytics.spark.metadata

import com.thinkbiganalytics.spark.rest.model.TransformResponse

import org.apache.spark.SparkContext
import org.apache.spark.scheduler.StageInfo
import org.junit.{Assert, Test}
import org.mockito.Mockito

import java.util.concurrent.Callable

class TransformJobTest {

    /** Verify the job progress calculation. */
    @Test
    def progress(): Unit = {
        // Test with new job
        val job = new TransformJob("MyJob", Mockito.mock(classOf[Callable[TransformResponse]]), Mockito.mock(classOf[SparkContext]))
        Assert.assertEquals(0.0, job.progress, 0.0)

        // Test when job started
        job.stages = Seq(new StageInfo(1, 1, "stage-1", 25, Seq(), Seq(), ""), new StageInfo(2, 1, "stage-2", 75, Seq(), Seq(), ""))
        Assert.assertEquals(0.0, job.progress, 0.0)

        // Test when stage started
        job.onStageProgress(job.stages(0), 0)
        Assert.assertEquals(0.0, job.progress, 0.0)

        // Test when task completed
        job.onTaskEnd()
        Assert.assertEquals(0.01, job.progress, 0.001)

        job.onTaskEnd()
        Assert.assertEquals(0.02, job.progress, 0.001)

        // Test when stage completed
        job.onStageProgress(job.stages(0), 25)
        Assert.assertEquals(0.25, job.progress, 0.001)

        // Test when task completed
        job.onTaskEnd()
        Assert.assertEquals(0.26, job.progress, 0.001)

        // Test when stage failed
        job.onStageProgress(job.stages(1), 0)
        Assert.assertEquals(0.25, job.progress, 0.001)

        // Test when job completed
        job.onJobEnd()
        Assert.assertEquals(1.0, job.progress, 0.001)
    }

    /** Verify executing the job. */
    @Test
    def run(): Unit = {
        // Mock callable function and Spark context
        val response = new TransformResponse
        val callable = new Callable[TransformResponse] {
            override def call(): TransformResponse = response
        }

        val spark = Mockito.mock(classOf[SparkContext])

        // Test executing job
        val job = new TransformJob("MyJob", callable, spark)
        job.run()

        Assert.assertTrue(job.isDone)
        Mockito.verify(spark).setJobGroup("MyJob", "Transform Job", interruptOnCancel = false)
        Assert.assertEquals(response, job.get())
        Mockito.verify(spark).clearJobGroup()
    }
}
