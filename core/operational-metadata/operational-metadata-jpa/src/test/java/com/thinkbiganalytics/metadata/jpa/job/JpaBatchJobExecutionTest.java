package com.thinkbiganalytics.metadata.jpa.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import com.thinkbiganalytics.test.security.WithMockJaasUser;

import org.joda.time.Period;
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
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, OperationalMetadataConfig.class, TestJpaConfiguration.class})
public class JpaBatchJobExecutionTest {


    @Inject
    private BatchJobExecutionProvider jobExecutionProvider;

    @Inject
    private MetadataAccess operationalMetadataAccess;

    public static Map<String, Field> getFields(Class<?> cl) {
        return Arrays.asList(cl.getDeclaredFields()).stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testPaging() {
        operationalMetadataAccess.read(() -> {
            String filter = "jobInstance.feed.feedType==FEED,jobInstance.feed.name==movies.new_releases";
            //String feed = "movies.new_releases";
            Page<? extends BatchJobExecution> page = jobExecutionProvider.findAll(filter, new PageRequest(0, 10, Sort.Direction.DESC, "jobExecutionId"));
            return page;
        });
    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testJobStatusCount() {
        operationalMetadataAccess.read(() -> {
            List<JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDate();
            return counts;
        });

    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testJobStatusCountFromNow() {
        operationalMetadataAccess.read(() -> {
            Period period = DateTimeUtil.period("3Y");
            List<JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDateFromNow(period, null);
            return counts;
        });

    }

    @WithMockJaasUser(username = "dladmin",
                      password = "secret",
                      authorities = {"admin"})
    @Test
    public void testFilters() {
        operationalMetadataAccess.read(() -> {
            QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
            BooleanBuilder builder = GenericQueryDslFilter.buildFilter(jobExecution, "status==\"COMPLETED,FAILED\"");
            int i = 0;
            return null;
        });

    }
}
