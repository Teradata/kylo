package com.thinkbiganalytics.jobrepo.batch.config;

import com.thinkbiganalytics.jobrepo.batch.scheduling.SchedulerService;
import com.thinkbiganalytics.jobrepo.batch.scheduling.concurrent.PausableThreadPoolTaskExecutor;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.TaskExecutor;

/**
 * Created by sr186054 on 4/14/16.
 */
public class PausableJobConfig {
    public PausableJobConfig() {
    }

    public static TaskExecutor createPausableThreadPoolExecutor(SchedulerService schedulerService, String beanName, int poolSize, int maxPoolSize, int queueCapacity) {
        PausableThreadPoolTaskExecutor executor = new PausableThreadPoolTaskExecutor();
        executor.setSchedulerService(schedulerService);
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setBeanName(beanName);
        executor.setThreadNamePrefix(beanName);
        executor.initialize();
        return executor;
    }

    public static JobLauncher createPausableJobLauncher(JobRepository jobRepository, TaskExecutor taskExecutor) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(taskExecutor);
        return launcher;
    }
}
