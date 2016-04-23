package com.thinkbiganalytics.jobrepo.batch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableBatchProcessing
public class BatchJobsConfiguration {

    @Inject
    public JobRepository jobRepository;

    @Inject
    public JobRegistry jobRegistryBeanPostProcessor;

    @Inject
    public JobExplorer jobExplorer;


    @Value("${taskExecutor.maxPoolSize:50}")
    protected Integer maxPoolSize;

    @Value("${taskExecutor.corePoolSize:25}")
    protected Integer corePoolSize;

    @Value("${taskExecutor.queueCapacity:1000}")
    protected Integer queueCapacity;

    @Value("${taskExecutor.waitForTaskesToCompleteOnShutdown:true}")
    protected boolean waitForTaskesToCompleteOnShutdown;


    /**
     * Create a job registry such that we can see and find jobs by name
     * @param jobRegistry The job registry created by spring boot
     * @return The job registry
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(final JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public JobOperator jobOperator() {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(this.jobExplorer);
        jobOperator.setJobLauncher(this.jobLauncher());
        jobOperator.setJobRegistry(this.jobRegistryBeanPostProcessor);
        jobOperator.setJobRepository(this.jobRepository);
        return jobOperator;
    }


    @Bean(name="jobLauncher")
    public JobLauncher jobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor());
        return jobLauncher;
    }

    @Bean(name="taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(waitForTaskesToCompleteOnShutdown);
        return executor;
    }

}