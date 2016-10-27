package com.thinkbiganalytics;



import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.thinkbiganalytics.ui.config.SpringJerseyConfiguration;

@Configuration
@SpringBootApplication(exclude = {VelocityAutoConfiguration.class, DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class,
                                  DataSourceTransactionManagerAutoConfiguration.class })
//@EnableAutoConfiguration(exclude = {VelocityAutoConfiguration.class, DataSourceAutoConfiguration.class })
@EnableConfigurationProperties
@Import({SpringJerseyConfiguration.class})
@ComponentScan("com.thinkbiganalytics")
@EnableZuulProxy
public class ThinkbigDataLakeUiApplication implements SchedulingConfigurer {


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
