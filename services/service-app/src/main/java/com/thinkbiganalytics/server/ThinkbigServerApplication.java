package com.thinkbiganalytics.server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.rest.SpringJerseyConfiguration;

@Configuration
@SpringBootApplication
@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class})
@EnableConfigurationProperties
@EnableConfigServer
@Import({DatabaseConfiguration.class, OperationalMetadataConfig.class,SpringJerseyConfiguration.class})
@ComponentScan("com.thinkbiganalytics") //FIX this
public class ThinkbigServerApplication implements SchedulingConfigurer {


  @Bean(destroyMethod = "shutdown")
  public Executor scheduledTaskExecutor() {
    return Executors.newScheduledThreadPool(25);
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
    scheduledTaskRegistrar.setScheduler(scheduledTaskExecutor());
  }


  public static void main(String[] args) {
    SpringApplication.run("classpath:application-context.xml", args);
  }


}
