package com.thinkbiganalytics.jobrepo.batch.config;

import com.thinkbiganalytics.jobrepo.batch.scheduling.SchedulerService;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import javax.inject.Inject;

/**
 * Configuration for the job launcher used to create new jobs
 */
@Configuration
public class CreateJobLauncherConfig {
    public final static String PIPELINE_CONTROLLER_THREAD_POOL = "CreateJobThreadPool";
    public final static String PIPELINE_CONTROLLER_JOB_LAUNCHER = "PipelineControllerJobLauncher";

    @Inject
    private SchedulerService schedulerService;

    /**
     * Declare a thread pool that only allows one historical job at a time
     *
     * @return An initialized thread pool to run historical jobs on
     */
    @Bean
    public TaskExecutor pipelineControllerThreadPoolExecutor() {

        return PausableJobConfig.createPausableThreadPoolExecutor(this.schedulerService, PIPELINE_CONTROLLER_JOB_LAUNCHER, 1, 1, 100);

        //Standard TaskExecutor
/*
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(100);
		executor.setBeanName(PIPELINE_CONTROLLER_THREAD_POOL);
		executor.setThreadNamePrefix("CreatedJob");
		executor.initialize();


		return executor;
		*/
    }

    /**
     * Declare a job launcher for running detailed jobs that is tied to an asynchronous thread pool that we
     * can adjust to specify how many of these may be run in parallel
     *
     * @param jobRepository                        The repository to use for persisting job information
     * @param pipelineControllerThreadPoolExecutor The executor specifying the concurrency level of these jobs
     * @return An instance of an async job launcher
     */
    @Bean(name = PIPELINE_CONTROLLER_JOB_LAUNCHER)
    public JobLauncher pipelineControllerJobLauncher(
            final JobRepository jobRepository,
            final TaskExecutor pipelineControllerThreadPoolExecutor) {
        return PausableJobConfig.createPausableJobLauncher(jobRepository, pipelineControllerThreadPoolExecutor);
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
