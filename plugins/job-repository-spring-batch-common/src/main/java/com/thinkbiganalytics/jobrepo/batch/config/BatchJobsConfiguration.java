package com.thinkbiganalytics.jobrepo.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 *
 * @author Sean Felten
 *
 * added Transaction Manager support
 */
@Configuration
@EnableBatchProcessing
public class BatchJobsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchJobsConfiguration.class);
    @Inject
    public JobRegistry jobRegistryBeanPostProcessor;



    @Value("${taskExecutor.maxPoolSize:50}")
    protected Integer maxPoolSize;

    @Value("${taskExecutor.corePoolSize:25}")
    protected Integer corePoolSize;

    @Value("${taskExecutor.queueCapacity:1000}")
    protected Integer queueCapacity;

    @Value("${taskExecutor.waitForTasksToCompleteOnShutdown:true}")
    protected boolean waitForTasksToCompleteOnShutdown;




    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();
        return newDataSource;
    }

    @Bean(name="transactionManager")
    public PlatformTransactionManager transactionManager(){
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource());
        return transactionManager;
    }

    @Bean
    public JobRepository jobRepository() throws Exception{
        JobRepositoryFactoryBean jobRepository= new JobRepositoryFactoryBean();
        jobRepository.setDataSource(dataSource());
        jobRepository.setTransactionManager(transactionManager());
        jobRepository.afterPropertiesSet();
        log.info("Created new JobRepositorFactoryBean with datasource");
        return jobRepository.getObject();
    }

    @Bean
    public JobExplorer jobExplorer() throws Exception{
        JobExplorerFactoryBean jobExplorerFactoryBean  = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(dataSource());
        //jobExplorerFactoryBean.setSerializer();
        jobExplorerFactoryBean.setJdbcOperations(jdbcTemplate(dataSource()));
        jobExplorerFactoryBean.afterPropertiesSet();
        log.info("Created new jobExplorerFactoryBean with datasource");
        return jobExplorerFactoryBean.getObject();
    }



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
    public JobOperator jobOperator()  throws Exception{
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(this.jobExplorer());
        jobOperator.setJobLauncher(this.jobLauncher());
        jobOperator.setJobRegistry(this.jobRegistryBeanPostProcessor);
        jobOperator.setJobRepository(this.jobRepository());
        return jobOperator;
    }


    @Bean(name="jobLauncher")
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.setTaskExecutor(taskExecutor());
        return jobLauncher;
    }

    @Bean(name="taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        return executor;
    }

}