package com.thinkbiganalytics.metadata.jpa.job;

import com.querydsl.core.BooleanBuilder;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.config.OperationalMetadataConfig;
import com.thinkbiganalytics.metadata.jpa.TestJpaConfiguration;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 11/29/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
public class JpaBatchJobExecutionTest {


    @Inject
    private BatchJobExecutionProvider jobExecutionProvider;

    @Inject
    private MetadataAccess operationalMetadataAccess;

    @Test
    public void testPaging(){
        operationalMetadataAccess.read(() -> {
            String filter = "jobInstance.feed.feedType==FEED,jobInstance.feed.name==movies.new_releases";
            //String feed = "movies.new_releases";
          Page<? extends BatchJobExecution> page = jobExecutionProvider.findAll(filter, new PageRequest(0, 10, Sort.Direction.DESC,"jobExecutionId"));
          return page;
        });
    }

    @Test
    public void testJobStatusCount(){
        operationalMetadataAccess.read(() -> {
          List<JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDate();
            return counts;
        });

    }

    @Test
    public void testJobStatusCountFromNow(){
        operationalMetadataAccess.read(() -> {
            Period period = DateTimeUtil.period("3Y");
            List<JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDateFromNow(period,null);
            return counts;
        });

    }

    @Test
    public void testFilters(){
        operationalMetadataAccess.read(() -> {
            QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
            BooleanBuilder builder = GenericQueryDslFilter.buildFilter(jobExecution,"status==\"COMPLETED,FAILED\"");
            int i = 0;
        return null;
        });

    }



    public static Map<String,Field> getFields(Class<?> cl) {
         return Arrays.asList(cl.getDeclaredFields()).stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
    }
}
